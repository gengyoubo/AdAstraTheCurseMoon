package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.block.MoonDustGeneratorBlock;
import github.com.gengyoubo.adssc.block.MoonDustOreBlock;
import github.com.gengyoubo.adssc.pipe.EnergyPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.FluidPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.ItemPipeBlockEntity;
import github.com.gengyoubo.adssc.pipe.PipeBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.MapColor;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.BLOCK);

    public static final RegistrySupplier<Block> MOON_DUST_ORE = BLOCKS.register(
        "moon_dust_ore", () -> new MoonDustOreBlock(Properties.copy(Blocks.LAPIS_ORE)));

    public static final RegistrySupplier<Block> MOON_DUST_GENERATOR = BLOCKS.register(
        "moon_dust_generator", () -> new MoonDustGeneratorBlock(Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(3.5f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.METAL)
            .lightLevel(state -> state.getValue(MoonDustGeneratorBlock.LIT) ? 11 : 0)));

    public static final RegistrySupplier<Block> ITEM_PIPE = BLOCKS.register(
        "item_pipe", () -> new PipeBlock(pipeProperties(), ModBlockEntities.ITEM_PIPE, ItemPipeBlockEntity::isAcceptor,
            Component.translatable("tooltip.ad_ssc.item_pipe", ItemPipeBlockEntity.PULL_AMOUNT, ItemPipeBlockEntity.PULL_INTERVAL)));
    public static final RegistrySupplier<Block> FLUID_PIPE = BLOCKS.register(
        "fluid_pipe", () -> new PipeBlock(pipeProperties(), ModBlockEntities.FLUID_PIPE, FluidPipeBlockEntity::isAcceptor,
            Component.translatable("tooltip.ad_ssc.fluid_pipe", FluidPipeBlockEntity.PULL_MB)));
    public static final RegistrySupplier<Block> ENERGY_PIPE = BLOCKS.register(
        "energy_pipe", () -> new PipeBlock(pipeProperties(), ModBlockEntities.ENERGY_PIPE, EnergyPipeBlockEntity::isAcceptor,
            Component.translatable("tooltip.ad_ssc.energy_pipe", EnergyPipeBlockEntity.ENERGY_RATE, EnergyPipeBlockEntity.FLUX_RATE)));

    private ModBlocks() {
    }

    private static Properties pipeProperties() {
        return Properties.of()
            .mapColor(MapColor.QUARTZ)
            .strength(1.0f)
            .sound(SoundType.METAL)
            .noOcclusion()
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false);
    }

    public static void register() {
        BLOCKS.register();
    }
}
