package github.com.gengyoubo.adssc.compat;

import earth.terrarium.adastra.api.events.AdAstraEvents;
import github.com.gengyoubo.adssc.registry.ModDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class CurseMoonEnvironment {
    private CurseMoonEnvironment() {
    }

    public static void init() {
        AdAstraEvents.OxygenTickEvent.register((level, entity) -> !isProtected(entity));
        AdAstraEvents.TemperatureTickEvent.register((level, entity) -> !isProtected(entity));
    }

    public static boolean isProtected(LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        var dimension = player.level().dimension();
        if (!ModDimensions.isCurseMoonSystem(dimension)) return false;
        return SscCompat.isTransformed(player);
    }
}
