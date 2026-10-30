package github.com.gengyoubo.adssc.pipe;

import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import earth.terrarium.botarium.common.energy.impl.SimpleEnergyContainer;
import earth.terrarium.botarium.util.Updatable;
import github.com.gengyoubo.adssc.flux.MoonFlux;
import github.com.gengyoubo.adssc.flux.MoonFluxContainer;
import github.com.gengyoubo.adssc.flux.MoonFluxHolder;
import github.com.gengyoubo.adssc.flux.MoonFluxStorage;
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
 * Energy pipe with two separate channels: the loader's energy (Forge Energy, or Team Reborn Energy on Fabric, as used
 * by Ad Astra) through Botarium, and Moon Flux through {@link MoonFluxHolder}. The two never convert into each other;
 * each moves only between blocks that hold that kind.
 */
public class EnergyPipeBlockEntity extends PipeBlockEntity implements MoonFluxHolder {
    public static final int ENERGY_RATE = 4000;
    public static final int FLUX_RATE = 2000;

    private final SimpleEnergyContainer energy = new SimpleEnergyContainer(ENERGY_RATE, ENERGY_RATE, ENERGY_RATE);
    private final MoonFluxContainer flux = new MoonFluxContainer(FLUX_RATE, FLUX_RATE, FLUX_RATE, this::setChanged);
    private int energyInputs;
    private int fluxInputs;

    public EnergyPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_PIPE.get(), pos, state);
    }

    public static boolean isAcceptor(Level level, BlockPos pos, Direction face) {
        return MoonFlux.find(level, pos, face) != null || EnergyContainer.holdsEnergy(level, pos, face);
    }

    private record Target(EnergyPipeBlockEntity pipe, Direction side, PipeTransfer.Sink sink) {
    }

    @Override
    protected void tickNetwork(PipeNetwork network) {
        List<Target> energyTargets = null;
        List<Target> fluxTargets = null;
        for (PipeBlockEntity member : network.members()) {
            EnergyPipeBlockEntity pipe = (EnergyPipeBlockEntity) member;
            if (pipe.energy.getStoredEnergy() > 0) {
                if (energyTargets == null) energyTargets = energyTargets(network);
                long moved = PipeTransfer.split(sinks(energyTargets, pipe, pipe.energyInputs), pipe.energy.getStoredEnergy(), false);
                pipe.energy.internalExtract(moved, false);
                if (pipe.energy.getStoredEnergy() <= 0) pipe.energyInputs = 0;
                if (moved > 0) pipe.setChanged();
            }
            if (pipe.flux.getStored() > 0) {
                if (fluxTargets == null) fluxTargets = fluxTargets(network);
                long moved = PipeTransfer.split(sinks(fluxTargets, pipe, pipe.fluxInputs), pipe.flux.getStored(), false);
                pipe.flux.extract((int) moved, false);
                if (pipe.flux.getStored() <= 0) pipe.fluxInputs = 0;
            }
        }

        for (PipeBlockEntity member : network.members()) {
            EnergyPipeBlockEntity pipe = (EnergyPipeBlockEntity) member;
            for (Direction side : Direction.values()) {
                if (pipe.getMode(side) != PipeMode.PULL || !pipe.facesAcceptor(side)) continue;
                BlockPos sourcePos = pipe.worldPosition.relative(side);
                EnergyContainer energySource = EnergyContainer.of(level, sourcePos, side.getOpposite());
                if (energySource != null && energySource.allowsExtraction()) {
                    if (energyTargets == null) energyTargets = energyTargets(network);
                    List<PipeTransfer.Sink> sinks = sinks(energyTargets, pipe, bit(side));
                    long accepted = PipeTransfer.split(sinks, energySource.extractEnergy(ENERGY_RATE, true), true);
                    long taken = energySource.extractEnergy(accepted, false);
                    long moved = PipeTransfer.split(sinks, taken, false);
                    if (moved < taken) pipe.energy.internalInsert(taken - moved, false);
                }
                MoonFluxStorage fluxSource = MoonFlux.find(level, sourcePos, side.getOpposite());
                if (fluxSource != null && fluxSource.canExtract()) {
                    if (fluxTargets == null) fluxTargets = fluxTargets(network);
                    List<PipeTransfer.Sink> sinks = sinks(fluxTargets, pipe, bit(side));
                    long accepted = PipeTransfer.split(sinks, fluxSource.extract(FLUX_RATE, true), true);
                    int taken = fluxSource.extract((int) accepted, false);
                    long moved = PipeTransfer.split(sinks, taken, false);
                    if (moved < taken) pipe.flux.generate(taken - (int) moved);
                }
            }
        }
    }

    private List<Target> energyTargets(PipeNetwork network) {
        List<Target> targets = new ArrayList<>();
        for (PipeBlockEntity member : network.members()) {
            for (Direction side : Direction.values()) {
                if (!member.getMode(side).canOutput() || !member.facesAcceptor(side)) continue;
                EnergyContainer container = EnergyContainer.of(level, member.getBlockPos().relative(side), side.getOpposite());
                if (container != null && container.allowsInsertion()) {
                    targets.add(new Target((EnergyPipeBlockEntity) member, side, container::insertEnergy));
                }
            }
        }
        return targets;
    }

    private List<Target> fluxTargets(PipeNetwork network) {
        List<Target> targets = new ArrayList<>();
        for (PipeBlockEntity member : network.members()) {
            for (Direction side : Direction.values()) {
                if (!member.getMode(side).canOutput() || !member.facesAcceptor(side)) continue;
                MoonFluxStorage storage = MoonFlux.find(level, member.getBlockPos().relative(side), side.getOpposite());
                if (storage != null && storage.canInsert()) {
                    targets.add(new Target((EnergyPipeBlockEntity) member, side,
                        (amount, simulate) -> storage.insert((int) Math.min(amount, Integer.MAX_VALUE), simulate)));
                }
            }
        }
        return targets;
    }

    /** The targets' sinks, leaving out the faces in {@code skipSides} of {@code from}. */
    private static List<PipeTransfer.Sink> sinks(List<Target> targets, EnergyPipeBlockEntity from, int skipSides) {
        List<PipeTransfer.Sink> sinks = new ArrayList<>(targets.size());
        for (Target target : targets) {
            if (target.pipe != from || (skipSides & bit(target.side)) == 0) sinks.add(target.sink);
        }
        return sinks;
    }

    @Override
    @Nullable
    public MoonFluxStorage getMoonFlux(@Nullable Direction side) {
        if (side == null) return flux;
        return getMode(side) != PipeMode.NONE ? new FluxFace(side) : null;
    }

    /** The energy buffer as seen through {@code side}, or null if that face is closed. */
    @Nullable
    public EnergyFace energyFace(@Nullable Direction side) {
        return side == null || getMode(side) != PipeMode.NONE ? new EnergyFace(side) : null;
    }

    /** One face of the Moon Flux buffer: insertion follows the face's mode and nothing can be extracted. */
    private final class FluxFace implements MoonFluxStorage {
        private final Direction side;

        private FluxFace(Direction side) {
            this.side = side;
        }

        @Override
        public int getStored() {
            return flux.getStored();
        }

        @Override
        public int getCapacity() {
            return flux.getCapacity();
        }

        @Override
        public int insert(int amount, boolean simulate) {
            if (!canInsert()) return 0;
            int inserted = flux.insert(amount, simulate);
            if (!simulate && inserted > 0) fluxInputs |= bit(side);
            return inserted;
        }

        @Override
        public int extract(int amount, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canInsert() {
            return getMode(side).canInput();
        }

        @Override
        public boolean canExtract() {
            return false;
        }
    }

    /** One face of the energy buffer: insertion follows the face's mode and nothing can be extracted. */
    public final class EnergyFace implements EnergyContainer, Updatable<BlockEntity> {
        @Nullable
        private final Direction side;

        private EnergyFace(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public long insertEnergy(long maxAmount, boolean simulate) {
            if (!allowsInsertion()) return 0;
            long inserted = energy.insertEnergy(maxAmount, simulate);
            if (!simulate && inserted > 0) {
                energyInputs |= bit(side);
                setChanged();
            }
            return inserted;
        }

        @Override
        public long extractEnergy(long maxAmount, boolean simulate) {
            return 0;
        }

        @Override
        public void setEnergy(long amount) {
            energy.setEnergy(amount);
        }

        @Override
        public long getStoredEnergy() {
            return energy.getStoredEnergy();
        }

        @Override
        public long getMaxCapacity() {
            return energy.getMaxCapacity();
        }

        @Override
        public long maxInsert() {
            return energy.maxInsert();
        }

        @Override
        public long maxExtract() {
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
        public EnergySnapshot createSnapshot() {
            return energy.createSnapshot();
        }

        @Override
        public void readSnapshot(EnergySnapshot snapshot) {
            energy.readSnapshot(snapshot);
        }

        @Override
        public void deserialize(CompoundTag tag) {
            energy.deserialize(tag);
        }

        @Override
        public CompoundTag serialize(CompoundTag tag) {
            return energy.serialize(tag);
        }

        @Override
        public void clearContent() {
            energy.clearContent();
        }

        @Override
        public void update(BlockEntity entity) {
            setChanged();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        energy.setEnergy(Math.max(0, Math.min(tag.getLong("Energy"), energy.getMaxCapacity())));
        flux.load(tag);
        energyInputs = tag.getByte("EnergyInputSides");
        fluxInputs = tag.getByte("FluxInputSides");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("Energy", energy.getStoredEnergy());
        flux.save(tag);
        tag.putByte("EnergyInputSides", (byte) energyInputs);
        tag.putByte("FluxInputSides", (byte) fluxInputs);
    }
}
