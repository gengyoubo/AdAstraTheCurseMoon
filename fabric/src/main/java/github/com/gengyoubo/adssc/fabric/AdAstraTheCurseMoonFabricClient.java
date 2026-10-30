package github.com.gengyoubo.adssc.fabric;

import github.com.gengyoubo.adssc.client.AdSscClientArmor;
import github.com.gengyoubo.adssc.client.AdSscClientPlanets;
import github.com.gengyoubo.adssc.client.AdSscClientScreens;
import github.com.gengyoubo.adssc.client.CurseSkyClient;
import net.fabricmc.api.ClientModInitializer;

public final class AdAstraTheCurseMoonFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AdSscClientArmor.init();
        AdSscClientPlanets.init();
        AdSscClientScreens.init();
        CurseSkyClient.init();
    }
}
