package github.com.gengyoubo.adssc.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Shared state of every pipe: the connection mode of each face and the network it belongs to.
 * <p>
 * Subclasses own a small buffer that neighbours can push into through faces that accept input, and move contents
 * across the whole network in {@link #tickNetwork}. A buffer remembers which faces filled it so its contents are not
 * handed straight back to the block they came from.
 */
public abstract class PipeBlockEntity extends BlockEntity {
    private final PipeMode[] modes = new PipeMode[6];
    @Nullable
    PipeNetwork network;
    /** Faces linked to another pipe at the last refresh, or -1 before the first one. */
    private int linkMask = -1;

    protected PipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        Arrays.fill(modes, PipeMode.NORMAL);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PipeBlockEntity pipe) {
        PipeNetwork network = pipe.network != null ? pipe.network : PipeNetwork.build(pipe);
        if (network.claimTick(level.getGameTime())) pipe.tickNetwork(network);
    }

    /** Moves contents for the whole network; runs once per tick on whichever member ticks first. */
    protected abstract void tickNetwork(PipeNetwork network);

    public PipeMode getMode(Direction side) {
        return modes[side.get3DDataValue()];
    }

    /** Advances the mode of {@code side}; faces towards another pipe only toggle between linked and not. */
    public PipeMode cycleMode(Direction side, boolean forward) {
        PipeBlockEntity neighbour = neighbourPipe(side);
        PipeMode mode = getMode(side).cycle(forward, neighbour != null);
        modes[side.get3DDataValue()] = mode;
        setChanged();
        PipePlatform.onSidesChanged(this);
        refresh();
        if (neighbour != null) neighbour.refresh();
        return mode;
    }

    /** Re-reads the neighbours: rebuilds the network if links changed and updates the drawn arms. */
    public void refresh() {
        if (level == null || level.isClientSide || isRemoved()) return;
        int links = 0;
        for (Direction direction : Direction.values()) {
            if (linkedPipe(direction) != null) links |= bit(direction);
        }
        if (links != linkMask) {
            linkMask = links;
            invalidateNetwork();
        }
        BlockState state = getBlockState();
        if (state.getBlock() instanceof PipeBlock block) {
            BlockState updated = block.withSides(state, level, worldPosition, this);
            if (updated != state) level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        }
    }

    /** The loaded pipe of the same kind next to {@code side}, linked or not. */
    @Nullable
    public PipeBlockEntity neighbourPipe(Direction side) {
        if (level == null) return null;
        BlockPos pos = worldPosition.relative(side);
        if (!level.isLoaded(pos)) return null;
        return level.getBlockEntity(pos) instanceof PipeBlockEntity pipe && pipe.getType() == getType() && !pipe.isRemoved()
            ? pipe : null;
    }

    /** The neighbouring pipe if neither face is set to {@link PipeMode#NONE}. */
    @Nullable
    public PipeBlockEntity linkedPipe(Direction side) {
        if (getMode(side) == PipeMode.NONE) return null;
        PipeBlockEntity pipe = neighbourPipe(side);
        return pipe != null && pipe.getMode(side.getOpposite()) != PipeMode.NONE ? pipe : null;
    }

    /** Whether {@code side} leads to a block other than a pipe that the model shows an arm to. */
    protected boolean facesAcceptor(Direction side) {
        return getBlockState().getBlock() instanceof PipeBlock
            && getBlockState().getValue(PipeBlock.property(side)) != PipeSide.NONE
            && neighbourPipe(side) == null;
    }

    protected static int bit(Direction side) {
        return 1 << side.get3DDataValue();
    }

    private void invalidateNetwork() {
        if (network != null) network.invalidate();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        invalidateNetwork();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        linkMask = -1;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        byte[] saved = tag.getByteArray("Modes");
        for (int i = 0; i < modes.length; i++) {
            modes[i] = i < saved.length ? PipeMode.byId(saved[i]) : PipeMode.NORMAL;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        byte[] saved = new byte[modes.length];
        for (int i = 0; i < modes.length; i++) {
            saved[i] = (byte) modes[i].ordinal();
        }
        tag.putByteArray("Modes", saved);
    }
}
