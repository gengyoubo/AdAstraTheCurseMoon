package github.com.gengyoubo.adssc.fabric;

import github.com.gengyoubo.adssc.client.AdSscClientArmor;
import net.fabricmc.api.ClientModInitializer;

public final class AdAstraTheCurseMoonFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AdSscClientArmor.init();
    }
}
