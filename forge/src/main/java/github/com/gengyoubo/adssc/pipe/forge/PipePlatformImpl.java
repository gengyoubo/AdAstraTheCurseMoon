package github.com.gengyoubo.adssc.pipe.forge;

import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.pipe.EnergyPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.FluidPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.ItemPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.ItemPort;
import github.com.gengyoubo.adssc.pipe.PipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.PipeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public final class PipePlatformImpl {
    private static final ResourceLocation CAPABILITY_ID = new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, "pipe");

    private PipePlatformImpl() {
    }

    /** The item handler capability, falling back to vanilla containers that do not provide one. */
    @Nullable
    public static ItemPort findItemPort(Level level, BlockPos pos, Direction side) {
        BlockEntity entity = level.getBlockEntity(pos);
        if (entity != null) {
            Optional<IItemHandler> handler = entity.getCapability(ForgeCapabilities.ITEM_HANDLER, side).resolve();
            if (handler.isPresent()) return new ForgeItemPort(handler.get());
        }
        BlockState state = level.getBlockState(pos);
        Container container = null;
        if (state.getBlock() instanceof WorldlyContainerHolder holder) {
            container = holder.getContainer(state, level, pos);
        } else if (entity instanceof Container found) {
            container = state.getBlock() instanceof ChestBlock chest ? ChestBlock.getContainer(chest, state, level, pos, true) : found;
        }
        if (container == null) return null;
        return new ForgeItemPort(container instanceof WorldlyContainer worldly ? new SidedInvWrapper(worldly, side) : new InvWrapper(container));
    }

    public static void registerStorages() {
        MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, PipePlatformImpl::attach);
    }

    /** Fires the invalidation listeners, so neighbours drop handles to faces that just opened or closed. */
    public static void onSidesChanged(PipeBlockEntity pipe) {
        pipe.invalidateCaps();
        pipe.reviveCaps();
    }

    /** Runs inside the block entity's constructor, so the handlers may only read the pipe lazily. */
    private static void attach(AttachCapabilitiesEvent<BlockEntity> event) {
        SidedCapability provider;
        if (event.getObject() instanceof ItemPipeBlockEntity pipe) {
            provider = new SidedCapability(ForgeCapabilities.ITEM_HANDLER,
                side -> pipe.getMode(side) != PipeMode.NONE ? new SidedInvWrapper(pipe, side) : null);
        } else if (event.getObject() instanceof FluidPipeBlockEntity pipe) {
            provider = new SidedCapability(ForgeCapabilities.FLUID_HANDLER, side -> {
                FluidPipeBlockEntity.Face face = pipe.face(side);
                return face != null ? new FluidFaceHandler(face) : null;
            });
        } else if (event.getObject() instanceof EnergyPipeBlockEntity pipe) {
            provider = new SidedCapability(ForgeCapabilities.ENERGY, side -> {
                EnergyPipeBlockEntity.EnergyFace face = pipe.energyFace(side);
                return face != null ? new EnergyFaceHandler(face) : null;
            });
        } else {
            return;
        }
        event.addCapability(CAPABILITY_ID, provider);
        event.addListener(provider::invalidate);
    }

    /** One capability per face, created on first request and cached until the pipe's faces change. */
    private static final class SidedCapability implements ICapabilityProvider {
        private final Capability<?> capability;
        private final Function<Direction, Object> factory;
        private final LazyOptional<?>[] cache = new LazyOptional<?>[6];

        private SidedCapability(Capability<?> capability, Function<Direction, Object> factory) {
            this.capability = capability;
            this.factory = factory;
        }

        @Override
        public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> requested, @Nullable Direction side) {
            if (requested != capability || side == null) return LazyOptional.empty();
            int index = side.get3DDataValue();
            if (cache[index] == null) {
                Object handler = factory.apply(side);
                cache[index] = handler != null ? LazyOptional.of(() -> handler) : LazyOptional.empty();
            }
            return cache[index].cast();
        }

        private void invalidate() {
            for (int i = 0; i < cache.length; i++) {
                if (cache[i] != null) cache[i].invalidate();
                cache[i] = null;
            }
        }
    }

    private record ForgeItemPort(IItemHandler handler) implements ItemPort {
        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            return ItemHandlerHelper.insertItemStacked(handler, stack, simulate);
        }

        @Override
        public ItemStack extract(Predicate<ItemStack> filter, int max, boolean simulate) {
            for (int slot = 0; slot < handler.getSlots() && max > 0; slot++) {
                ItemStack stored = handler.getStackInSlot(slot);
                if (stored.isEmpty() || !filter.test(stored.copy())) continue;
                ItemStack extracted = handler.extractItem(slot, max, simulate);
                if (!extracted.isEmpty()) return extracted;
            }
            return ItemStack.EMPTY;
        }
    }

    private record FluidFaceHandler(FluidPipeBlockEntity.Face face) implements IFluidHandler {
        @Override
        public int getTanks() {
            return face.getSize();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            FluidHolder fluid = face.getFluids().get(tank);
            return fluid.isEmpty() ? FluidStack.EMPTY : new FluidStack(fluid.getFluid(), (int) fluid.getFluidAmount(), fluid.getCompound());
        }

        @Override
        public int getTankCapacity(int tank) {
            return (int) Math.min(face.getTankCapacity(tank), Integer.MAX_VALUE);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return face.allowsInsertion();
        }

        @Override
        public int fill(FluidStack stack, FluidAction action) {
            if (stack.isEmpty()) return 0;
            return (int) face.insertFluid(FluidHolder.of(stack.getFluid(), stack.getAmount(), stack.getTag()), action.simulate());
        }

        @Override
        public @NotNull FluidStack drain(FluidStack stack, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }

    private record EnergyFaceHandler(EnergyPipeBlockEntity.EnergyFace face) implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return (int) face.insertEnergy(maxReceive, simulate);
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return (int) Math.min(face.getStoredEnergy(), Integer.MAX_VALUE);
        }

        @Override
        public int getMaxEnergyStored() {
            return (int) Math.min(face.getMaxCapacity(), Integer.MAX_VALUE);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return face.allowsInsertion();
        }
    }
}
