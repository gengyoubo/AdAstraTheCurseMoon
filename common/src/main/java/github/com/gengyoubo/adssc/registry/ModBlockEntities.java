package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.block.MoonDustGeneratorBlockEntity;
import github.com.gengyoubo.adssc.pipe.EnergyPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.FluidPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.ItemPipeBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistrySupplier<BlockEntityType<MoonDustGeneratorBlockEntity>> MOON_DUST_GENERATOR = BLOCK_ENTITIES.register(
        "moon_dust_generator",
        () -> BlockEntityType.Builder.of(MoonDustGeneratorBlockEntity::new, ModBlocks.MOON_DUST_GENERATOR.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistrySupplier<BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE = BLOCK_ENTITIES.register(
        "item_pipe", () -> BlockEntityType.Builder.of(ItemPipeBlockEntity::new, ModBlocks.ITEM_PIPE.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistrySupplier<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE = BLOCK_ENTITIES.register(
        "fluid_pipe", () -> BlockEntityType.Builder.of(FluidPipeBlockEntity::new, ModBlocks.FLUID_PIPE.get()).build(null));
    @SuppressWarnings("DataFlowIssue")
    public static final RegistrySupplier<BlockEntityType<EnergyPipeBlockEntity>> ENERGY_PIPE = BLOCK_ENTITIES.register(
        "energy_pipe", () -> BlockEntityType.Builder.of(EnergyPipeBlockEntity::new, ModBlocks.ENERGY_PIPE.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
