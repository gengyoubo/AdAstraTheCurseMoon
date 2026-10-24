package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.block.MoonDustOreBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> MOON_DUST_ORE = BLOCKS.register(
        "moon_dust_ore", () -> new MoonDustOreBlock(Properties.copy(Blocks.LAPIS_ORE)));

    private ModBlocks() {
    }

    public static void register() {
        BLOCKS.register();
    }
}
