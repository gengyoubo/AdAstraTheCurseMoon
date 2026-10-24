package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.CreativeTabRegistry;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.CREATIVE_MODE_TAB);

    public static final RegistrySupplier<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeTabRegistry.create(
        builder -> builder
            .title(Component.translatable("itemGroup.ad_ssc.main"))
            .icon(() -> new ItemStack(ModItems.MORPHSCALE_NETHERITE_SPACE_SUIT.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.MORPHSCALE_NETHERITE_SPACE_HELMET.get());
                output.accept(ModItems.MORPHSCALE_NETHERITE_SPACE_SUIT.get());
                output.accept(ModItems.MORPHSCALE_NETHERITE_SPACE_PANTS.get());
                output.accept(ModItems.MORPHSCALE_NETHERITE_SPACE_BOOTS.get());
                output.accept(ModItems.MORPHSCALE_JET_SUIT_HELMET.get());
                output.accept(ModItems.MORPHSCALE_JET_SUIT.get());
                output.accept(ModItems.MORPHSCALE_JET_SUIT_PANTS.get());
                output.accept(ModItems.MORPHSCALE_JET_SUIT_BOOTS.get());
                output.accept(ModItems.MOON_DUST_ORE.get());
                output.accept(ModItems.MOON_DUST_GENERATOR.get());
            })));

    private ModCreativeTabs() {
    }

    public static void register() {
        TABS.register();
    }
}
