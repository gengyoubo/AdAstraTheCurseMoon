package github.com.gengyoubo.adssc.compat;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.player.Player;

/** Keeps the two SSC implementations out of the shared module. */
public final class SscCompat {
    private SscCompat() {
    }

    @ExpectPlatform
    public static boolean isTransformed(Player player) {
        throw new AssertionError("Platform implementation missing");
    }
}
