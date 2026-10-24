package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.block.MoonDustGeneratorBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    @SuppressWarnings("DataFlowIssue")
    public static final RegistrySupplier<BlockEntityType<MoonDustGeneratorBlockEntity>> MOON_DUST_GENERATOR = BLOCK_ENTITIES.register(
        "moon_dust_generator",
        () -> BlockEntityType.Builder.of(MoonDustGeneratorBlockEntity::new, ModBlocks.MOON_DUST_GENERATOR.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register() {
        BLOCK_ENTITIES.register();
    }
}
