package github.com.gengyoubo.adssc.pipe;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * A 6-pixel pipe whose arms follow the {@link PipeSide} of each face.
 * <p>
 * The block state only describes the look; the modes live in the {@link PipeBlockEntity} and are configured with the
 * {@link PipeConfiguratorItem}.
 */
public class PipeBlock extends BaseEntityBlock {
    private static final Map<Direction, EnumProperty<PipeSide>> PROPERTIES = new EnumMap<>(Direction.class);
    private static final VoxelShape CORE = box(5, 5, 5, 11, 11, 11);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> RINGS = new EnumMap<>(Direction.class);
    /** Half the core's width, in blocks; a hit further from the centre than this lands on an arm. */
    private static final double CORE_RADIUS = 3 / 16.0;

    static {
        for (Direction direction : Direction.values()) {
            PROPERTIES.put(direction, EnumProperty.create(direction.getName(), PipeSide.class));
            ARMS.put(direction, alongAxis(direction, 5, 11, 0, 5));
            RINGS.put(direction, alongAxis(direction, 4, 12, 0, 2));
        }
    }

    /** Tells whether the block at {@code pos} exposes something this pipe carries on its {@code face}. */
    @FunctionalInterface
    public interface AcceptorTest {
        boolean test(Level level, BlockPos pos, Direction face);
    }

    private final Supplier<? extends BlockEntityType<? extends PipeBlockEntity>> type;
    private final AcceptorTest acceptors;
    private final Component tooltip;
    private final Map<BlockState, VoxelShape> shapes = new ConcurrentHashMap<>();

    public PipeBlock(Properties properties, Supplier<? extends BlockEntityType<? extends PipeBlockEntity>> type,
                     AcceptorTest acceptors, Component tooltip) {
        super(properties);
        this.type = type;
        this.acceptors = acceptors;
        this.tooltip = tooltip;
        BlockState state = stateDefinition.any();
        for (EnumProperty<PipeSide> property : PROPERTIES.values()) {
            state = state.setValue(property, PipeSide.NONE);
        }
        registerDefaultState(state);
    }

    public static EnumProperty<PipeSide> property(Direction side) {
        return PROPERTIES.get(side);
    }

    /** A box spanning {@code from..to} across the axis of {@code side}, and {@code near..far} pixels in from that face. */
    private static VoxelShape alongAxis(Direction side, double from, double to, double near, double far) {
        return switch (side) {
            case NORTH -> box(from, from, near, to, to, far);
            case SOUTH -> box(from, from, 16 - far, to, to, 16 - near);
            case WEST -> box(near, from, from, far, to, to);
            case EAST -> box(16 - far, from, from, 16 - near, to, to);
            case DOWN -> box(from, near, from, to, far, to);
            case UP -> box(from, 16 - far, from, to, 16 - near, to);
        };
    }

    /**
     * The face a click on a pipe configures: the arm that was hit, or the clicked face of the core.
     *
     * @param hit the clicked point in world coordinates
     */
    public static Direction targetSide(BlockPos pos, Vec3 hit, Direction clickedFace) {
        double dx = hit.x - pos.getX() - 0.5;
        double dy = hit.y - pos.getY() - 0.5;
        double dz = hit.z - pos.getZ() - 0.5;
        double ax = Math.abs(dx), ay = Math.abs(dy), az = Math.abs(dz);
        double furthest = Math.max(ax, Math.max(ay, az));
        if (furthest <= CORE_RADIUS + 1.0E-4) return clickedFace;
        if (furthest == ax) return dx > 0 ? Direction.EAST : Direction.WEST;
        if (furthest == ay) return dy > 0 ? Direction.UP : Direction.DOWN;
        return dz > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    /** {@code state} with each face's look recomputed from the neighbours and, if present, the pipe's modes. */
    public BlockState withSides(BlockState state, Level level, BlockPos pos, @Nullable PipeBlockEntity pipe) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(property(direction), sideLook(level, pos, direction, pipe));
        }
        return state;
    }

    private PipeSide sideLook(Level level, BlockPos pos, Direction direction, @Nullable PipeBlockEntity pipe) {
        PipeMode mode = pipe != null ? pipe.getMode(direction) : PipeMode.NORMAL;
        if (mode == PipeMode.NONE) return PipeSide.NONE;
        BlockPos neighbourPos = pos.relative(direction);
        if (!level.isLoaded(neighbourPos)) return PipeSide.NONE;
        if (level.getBlockState(neighbourPos).is(this)) {
            return level.getBlockEntity(neighbourPos) instanceof PipeBlockEntity neighbour
                && neighbour.getMode(direction.getOpposite()) != PipeMode.NONE ? PipeSide.CONNECTED : PipeSide.NONE;
        }
        return acceptors.test(level, neighbourPos, direction.getOpposite()) ? PipeSide.forAcceptor(mode) : PipeSide.NONE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        PROPERTIES.values().forEach(builder::add);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withSides(defaultBlockState(), context.getLevel(), context.getClickedPos(), null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        // The block entity does not exist yet; let it read its neighbours on the next tick.
        if (!oldState.is(this)) level.scheduleTick(pos, this, 1);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) pipe.refresh();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean moved) {
        super.neighborChanged(state, level, pos, block, fromPos, moved);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) pipe.refresh();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof Container container) {
            Containers.dropContents(level, pos, container);
        }
        super.onRemove(state, level, pos, newState, moved);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.computeIfAbsent(state, PipeBlock::buildShape);
    }

    private static VoxelShape buildShape(BlockState state) {
        VoxelShape shape = CORE;
        for (Direction direction : Direction.values()) {
            PipeSide side = state.getValue(property(direction));
            if (side != PipeSide.NONE) shape = Shapes.or(shape, ARMS.get(direction));
            if (side == PipeSide.PUSH || side == PipeSide.PULL) shape = Shapes.or(shape, RINGS.get(direction));
        }
        return shape.optimize();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.get().create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide || type != this.type.get() ? null
            : (tickLevel, pos, tickState, pipe) -> PipeBlockEntity.serverTick(tickLevel, pos, tickState, (PipeBlockEntity) pipe);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(this.tooltip.copy().withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ad_ssc.pipe.configure").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
