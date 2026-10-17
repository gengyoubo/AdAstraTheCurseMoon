package github.com.gengyoubo.adssc.compat.fabric;

import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.ability.RegPlayerFormComponent;

public final class SscCompatImpl {
    private SscCompatImpl() {
    }

    public static boolean isTransformed(Player player) {
        return RegPlayerFormComponent.PLAYER_FORM.maybeGet(player)
            .map(component -> component.getCurrentForm())
            .filter(form -> !form.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)
                && !form.equals(RegPlayerForms.ORIGINAL_SHIFTER))
            .isPresent();
    }
}
