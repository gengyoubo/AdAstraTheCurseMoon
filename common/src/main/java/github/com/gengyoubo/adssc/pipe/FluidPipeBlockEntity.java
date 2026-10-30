package github.com.gengyoubo.adssc.pipe;

import earth.terrarium.botarium.common.fluid.FluidConstants;
import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.FluidSnapshot;
import earth.terrarium.botarium.common.fluid.impl.SimpleFluidContainer;
import earth.terrarium.botarium.util.Updatable;
import github.com.gengyoubo.adssc.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluid pipe. Pull faces draw up to {@link #PULL_MB} mB per tick from their tank, and the network shares fluid evenly
 * between the tanks on push and normal faces. Lookups and the exposed buffer go through Botarium, so it works with
 * Ad Astra's machines and with Forge and Fabric fluid storages alike.
 */
public class FluidPipeBlockEntity extends PipeBlockEntity {
    public static final int PULL_MB = 1000;
    public static final int BUFFER_MB = 2000;

    private final SimpleFluidContainer buffer =
        new SimpleFluidContainer(FluidConstants.fromMillibuckets(BUFFER_MB), 1, (tank, fluid) -> true);
    private int inputSides;

    public FluidPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_PIPE.get(), pos, state);
    }

    public static boolean isAcceptor(Level level, BlockPos pos, Direction face) {
        return FluidContainer.holdsFluid(level, pos, face);
    }

    private record Target(FluidPipeBlockEntity pipe, Direction side, FluidContainer container) {
    }

    @Override
    protected void tickNetwork(PipeNetwork network) {
        List<Target> targets = null;
        for (PipeBlockEntity member : network.members()) {
            FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) member;
            FluidHolder held = pipe.buffer.getFirstFluid();
            if (held.isEmpty()) continue;
            if (targets == null) targets = targets(network);
            long moved = PipeTransfer.split(sinks(targets, held, pipe, pipe.inputSides), held.getFluidAmount(), false);
            if (moved <= 0) continue;
            pipe.buffer.internalExtract(FluidHolder.of(held.getFluid(), moved, held.getCompound()), false);
            if (pipe.buffer.getFirstFluid().isEmpty()) pipe.inputSides = 0;
            pipe.setChanged();
        }

        for (PipeBlockEntity member : network.members()) {
            FluidPipeBlockEntity pipe = (FluidPipeBlockEntity) member;
            for (Direction side : Direction.values()) {
                if (pipe.getMode(side) != PipeMode.PULL || !pipe.facesAcceptor(side)) continue;
                FluidContainer source = FluidContainer.of(level, pipe.worldPosition.relative(side), side.getOpposite());
                if (source == null || !source.allowsExtraction()) continue;
                if (targets == null) targets = targets(network);
                pipe.pull(targets, side, source);
            }
        }
    }

    private void pull(List<Target> targets, Direction side, FluidContainer source) {
        for (FluidHolder stored : List.copyOf(source.getFluids())) {
            if (stored.isEmpty()) continue;
            FluidHolder request = FluidHolder.of(stored.getFluid(), FluidConstants.fromMillibuckets(PULL_MB), stored.getCompound());
            long available = source.extractFluid(request, true).getFluidAmount();
            if (available <= 0) continue;
            List<PipeTransfer.Sink> sinks = sinks(targets, stored, this, bit(side));
            long accepted = PipeTransfer.split(sinks, available, true);
            if (accepted <= 0) continue;
            FluidHolder taken = source.extractFluid(FluidHolder.of(stored.getFluid(), accepted, stored.getCompound()), false);
            long moved = PipeTransfer.split(sinks, taken.getFluidAmount(), false);
            if (moved < taken.getFluidAmount()) {
                buffer.internalInsert(FluidHolder.of(taken.getFluid(), taken.getFluidAmount() - moved, taken.getCompound()), false);
                setChanged();
            }
            return;
        }
    }

    private List<Target> targets(PipeNetwork network) {
        List<Target> targets = new ArrayList<>();
        for (PipeBlockEntity member : network.members()) {
            for (Direction side : Direction.values()) {
                if (!member.getMode(side).canOutput() || !member.facesAcceptor(side)) continue;
                FluidContainer container = FluidContainer.of(level, member.getBlockPos().relative(side), side.getOpposite());
                if (container != null && container.allowsInsertion()) {
                    targets.add(new Target((FluidPipeBlockEntity) member, side, container));
                }
            }
        }
        return targets;
    }

    /** The targets as sinks for {@code fluid}, leaving out the faces in {@code skipSides} of {@code from}. */
    private static List<PipeTransfer.Sink> sinks(List<Target> targets, FluidHolder fluid, FluidPipeBlockEntity from, int skipSides) {
        List<PipeTransfer.Sink> sinks = new ArrayList<>(targets.size());
        for (Target target : targets) {
            if (target.pipe == from && (skipSides & bit(target.side)) != 0) continue;
            sinks.add((amount, simulate) ->
                target.container.insertFluid(FluidHolder.of(fluid.getFluid(), amount, fluid.getCompound()), simulate));
        }
        return sinks;
    }

    /** The buffer as seen through {@code side}, or null if that face is closed. */
    @Nullable
    public Face face(@Nullable Direction side) {
        return side == null || getMode(side) != PipeMode.NONE ? new Face(side) : null;
    }

    /** One face of the buffer: insertion follows the face's mode and nothing can be extracted. */
    public final class Face implements FluidContainer, Updatable<BlockEntity> {
        @Nullable
        private final Direction side;

        private Face(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public long insertFluid(FluidHolder fluid, boolean simulate) {
            if (!allowsInsertion()) return 0;
            long inserted = buffer.insertFluid(fluid, simulate);
            if (!simulate && inserted > 0) {
                inputSides |= bit(side);
                setChanged();
            }
            return inserted;
        }

        @Override
        public FluidHolder extractFluid(FluidHolder fluid, boolean simulate) {
            return FluidHolder.empty();
        }

        @Override
        public long extractFromSlot(FluidHolder fluidHolder, FluidHolder toExtract, Runnable snapshot) {
            return 0;
        }

        @Override
        public boolean allowsInsertion() {
            return side != null && getMode(side).canInput();
        }

        @Override
        public boolean allowsExtraction() {
            return false;
        }

        @Override
        public void setFluid(int slot, FluidHolder fluid) {
            buffer.setFluid(slot, fluid);
        }

        @Override
        public List<FluidHolder> getFluids() {
            return buffer.getFluids();
        }

        @Override
        public int getSize() {
            return buffer.getSize();
        }

        @Override
        public boolean isEmpty() {
            return buffer.isEmpty();
        }

        @Override
        public FluidContainer copy() {
            return buffer.copy();
        }

        @Override
        public long getTankCapacity(int tankSlot) {
            return buffer.getTankCapacity(tankSlot);
        }

        @Override
        public void fromContainer(FluidContainer container) {
            buffer.fromContainer(container);
        }

        @Override
        public FluidSnapshot createSnapshot() {
            return buffer.createSnapshot();
        }

        @Override
        public void readSnapshot(FluidSnapshot snapshot) {
            buffer.readSnapshot(snapshot);
        }

        @Override
        public void deserialize(CompoundTag tag) {
            buffer.deserialize(tag);
        }

        @Override
        public CompoundTag serialize(CompoundTag tag) {
            return buffer.serialize(tag);
        }

        @Override
        public void clearContent() {
            buffer.clearContent();
        }

        @Override
        public void update(BlockEntity entity) {
            setChanged();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        buffer.deserialize(tag);
        inputSides = tag.getByte("InputSides");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        buffer.serialize(tag);
        tag.putByte("InputSides", (byte) inputSides);
    }
}
