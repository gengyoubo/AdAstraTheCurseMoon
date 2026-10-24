package github.com.gengyoubo.adssc.client;

import dev.architectury.registry.menu.MenuRegistry;
import github.com.gengyoubo.adssc.registry.ModMenus;

public final class AdSscClientScreens {
    private AdSscClientScreens() {
    }

    public static void init() {
        MenuRegistry.registerScreenFactory(ModMenus.MOON_DUST_GENERATOR.get(), MoonDustGeneratorScreen::new);
    }
}
