package github.com.gengyoubo.adssc.compat;

import dev.architectury.injectables.annotations.ExpectPlatform;
import github.com.gengyoubo.adssc.radiation.RadiationExposure;
import net.minecraft.world.entity.player.Player;

/** Keeps the two SSC implementations out of the shared module. */
public final class SscCompat {
    private SscCompat() {
    }

    @ExpectPlatform
    public static boolean isTransformed(Player player) {
        throw new AssertionError("Platform implementation missing");
    }

    @ExpectPlatform
    public static RadiationExposure.Form radiationForm(Player player) {
        throw new AssertionError("Platform implementation missing");
    }

    @ExpectPlatform
    public static String radiationBase(Player player, String original) {
        throw new AssertionError("Platform implementation missing");
    }

    @ExpectPlatform
    public static String advanceRadiationForm(String base, int steps) {
        throw new AssertionError("Platform implementation missing");
    }

    @ExpectPlatform
    public static boolean applyRadiationForm(Player player, String id) {
        throw new AssertionError("Platform implementation missing");
    }
}
