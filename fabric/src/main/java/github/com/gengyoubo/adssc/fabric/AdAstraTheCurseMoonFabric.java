package github.com.gengyoubo.adssc.fabric;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import net.fabricmc.api.ModInitializer;

public final class AdAstraTheCurseMoonFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        AdAstraTheCurseMoon.init();
    }
}
