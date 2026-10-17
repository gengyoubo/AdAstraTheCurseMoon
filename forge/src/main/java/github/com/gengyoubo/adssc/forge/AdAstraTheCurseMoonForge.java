package github.com.gengyoubo.adssc.forge;

import dev.architectury.platform.forge.EventBuses;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AdAstraTheCurseMoon.MOD_ID)
public final class AdAstraTheCurseMoonForge {
    @SuppressWarnings("removal")
    public AdAstraTheCurseMoonForge() {
        EventBuses.registerModEventBus(AdAstraTheCurseMoon.MOD_ID,
            FMLJavaModLoadingContext.get().getModEventBus());
        AdAstraTheCurseMoon.init();
    }
}
