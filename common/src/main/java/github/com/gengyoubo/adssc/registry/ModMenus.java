package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.menu.MoonDustGeneratorMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

public final class ModMenus {
    private static final DeferredRegister<MenuType<?>> MENUS =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.MENU);

    public static final RegistrySupplier<MenuType<MoonDustGeneratorMenu>> MOON_DUST_GENERATOR = MENUS.register(
        "moon_dust_generator", () -> new MenuType<>(MoonDustGeneratorMenu::new, FeatureFlags.VANILLA_SET));

    private ModMenus() {
    }

    public static void register() {
        MENUS.register();
    }
}
