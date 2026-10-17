package github.com.gengyoubo.adssc.compat.forge;

import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseForge.api.SscApi;
import net.onixary.shapeShifterCurseForge.capability.PlayerFormData;

public final class SscCompatImpl {
    private SscCompatImpl() {
    }

    public static boolean isTransformed(Player player) {
        return SscApi.currentForm(player)
            .filter(data -> data.isContentEnabled())
            .map(data -> data.getFormId())
            .filter(id -> !id.isBlank()
                && !PlayerFormData.ORIGINAL_BEFORE_ENABLE_FORM.equals(id)
                && !PlayerFormData.ORIGINAL_SHIFTER_FORM.equals(id))
            .isPresent();
    }
}
