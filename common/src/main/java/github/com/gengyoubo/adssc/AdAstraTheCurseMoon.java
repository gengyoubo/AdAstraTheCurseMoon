package github.com.gengyoubo.adssc;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import github.com.gengyoubo.adssc.compat.CurseMoonEnvironment;
import github.com.gengyoubo.adssc.pipe.PipePlatform;
import github.com.gengyoubo.adssc.registry.ModBlockEntities;
import github.com.gengyoubo.adssc.registry.ModBlocks;
import github.com.gengyoubo.adssc.registry.ModCreativeTabs;
import github.com.gengyoubo.adssc.registry.ModItems;
import github.com.gengyoubo.adssc.registry.ModMenus;
import github.com.gengyoubo.adssc.registry.ModRecipes;
import github.com.gengyoubo.adssc.registry.ModEffects;
import github.com.gengyoubo.adssc.radiation.CurseMoonRadiation;
import github.com.gengyoubo.adssc.weather.CurseSkyWeather;
import org.slf4j.Logger;

public final class AdAstraTheCurseMoon {
    public static final String MOD_ID = "ad_ssc";
    public static final Logger LOGGER = LogUtils.getLogger();

    private AdAstraTheCurseMoon() {
    }

    public static void init() {
        LOGGER.info("Initializing {} on {}", MOD_ID, Platform.isFabric() ? "Fabric" : "Forge");
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModMenus.register();
        ModRecipes.register();
        ModCreativeTabs.register();
        ModEffects.register();
        PipePlatform.registerStorages();
        CurseMoonEnvironment.init();
        CurseMoonRadiation.init();
        CurseSkyWeather.init();
    }
}
