package github.com.gengyoubo.adssc.forge;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.client.AdSscClientArmor;
import github.com.gengyoubo.adssc.client.AdSscClientPlanets;
import github.com.gengyoubo.adssc.client.AdSscClientScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = AdAstraTheCurseMoon.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AdAstraTheCurseMoonForgeClient {
    private AdAstraTheCurseMoonForgeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(AdSscClientArmor::init);
        event.enqueueWork(AdSscClientPlanets::init);
        event.enqueueWork(AdSscClientScreens::init);
    }
}
