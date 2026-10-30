package github.com.gengyoubo.adssc.pipe.fabric;

import earth.terrarium.botarium.common.energy.EnergyApi;
import earth.terrarium.botarium.common.fluid.FluidApi;
import github.com.gengyoubo.adssc.pipe.EnergyPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.FluidPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.ItemPort;
import github.com.gengyoubo.adssc.pipe.PipeBlockEntity;
import github.com.gengyoubo.adssc.registry.ModBlockEntities;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

@SuppressWarnings("UnstableApiUsage")
public final class PipePlatformImpl {
    private PipePlatformImpl() {
    }

    @Nullable
    public static ItemPort findItemPort(Level level, BlockPos pos, Direction side) {
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(level, pos, side);
        return storage == null ? null : new FabricItemPort(storage);
    }

    /**
     * Botarium's getters receive the queried face and wrap the result with transaction support.
     * The item pipe needs nothing here: the Transfer API already wraps every {@code WorldlyContainer}.
     */
    public static void registerStorages() {
        FluidApi.BlockFluidGetter<FluidPipeBlockEntity.Face> fluids =
            (level, pos, state, entity, side) -> entity instanceof FluidPipeBlockEntity pipe ? pipe.face(side) : null;
        FluidApi.registerFluidBlockEntity(() -> ModBlockEntities.FLUID_PIPE.get(), fluids);
        EnergyApi.BlockEnergyGetter<EnergyPipeBlockEntity.EnergyFace> energy =
            (level, pos, state, entity, side) -> entity instanceof EnergyPipeBlockEntity pipe ? pipe.energyFace(side) : null;
        EnergyApi.registerEnergyBlockEntity(() -> ModBlockEntities.ENERGY_PIPE.get(), energy);
    }

    public static void onSidesChanged(PipeBlockEntity pipe) {
        // Lookups are made fresh on every query, so there is nothing cached to refresh.
    }

    /** Each call is its own outer transaction; the pipes only call in from their server tick. */
    private record FabricItemPort(Storage<ItemVariant> storage) implements ItemPort {
        @Override
        public ItemStack insert(ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) return ItemStack.EMPTY;
            try (Transaction transaction = Transaction.openOuter()) {
                long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
                if (!simulate) transaction.commit();
                return stack.copyWithCount(stack.getCount() - (int) inserted);
            }
        }

        @Override
        public ItemStack extract(Predicate<ItemStack> filter, int max, boolean simulate) {
            if (max <= 0) return ItemStack.EMPTY;
            // Collect first: the filter runs its own transactions, which must not overlap one held here.
            List<ItemVariant> kinds = new ArrayList<>();
            for (StorageView<ItemVariant> view : storage.nonEmptyViews()) {
                ItemVariant kind = view.getResource();
                if (!kind.isBlank() && !kinds.contains(kind)) kinds.add(kind);
            }
            for (ItemVariant kind : kinds) {
                if (!filter.test(kind.toStack(max))) continue;
                try (Transaction transaction = Transaction.openOuter()) {
                    long extracted = storage.extract(kind, max, transaction);
                    if (extracted <= 0) continue;
                    if (!simulate) transaction.commit();
                    return kind.toStack((int) extracted);
                }
            }
            return ItemStack.EMPTY;
        }
    }
}
