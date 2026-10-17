package github.com.gengyoubo.adssc;

import com.mojang.logging.LogUtils;
import dev.architectury.platform.Platform;
import github.com.gengyoubo.adssc.registry.ModItems;
import org.slf4j.Logger;

public final class AdAstraTheCurseMoon {
    public static final String MOD_ID = "ad_ssc";
    public static final Logger LOGGER = LogUtils.getLogger();

    private AdAstraTheCurseMoon() {
    }

    public static void init() {
        LOGGER.info("Initializing {} on {}", MOD_ID, Platform.isFabric() ? "Fabric" : "Forge");
        ModItems.register();
    }
}
