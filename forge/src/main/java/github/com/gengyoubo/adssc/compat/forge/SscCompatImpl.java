package github.com.gengyoubo.adssc.compat.forge;

import github.com.gengyoubo.adssc.radiation.RadiationExposure;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseForge.api.SscApi;
import net.onixary.shapeShifterCurseForge.capability.PlayerFormData;
import net.onixary.shapeShifterCurseForge.form.FormManager;
import net.onixary.shapeShifterCurseForge.form.FormRegistry;

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

    public static RadiationExposure.Form radiationForm(Player player) {
        return SscApi.currentForm(player).map(data -> {
            var form = FormRegistry.get(data.getFormId());
            int stage = isTransformed(player) && form != null ? Math.max(0, form.stage()) : 0;
            return new RadiationExposure.Form(data.getFormId(), stage, data.isContentEnabled());
        }).orElse(new RadiationExposure.Form(PlayerFormData.ORIGINAL_BEFORE_ENABLE_FORM, 0, false));
    }

    public static String radiationBase(Player player, String original) {
        if (!FormRegistry.ORIGINAL_SHIFTER.toString().equals(original)) return original;
        var starters = FormRegistry.forms().values().stream()
            .filter(form -> form.hasFlag("starter_form") && !form.hasFlag("sub_form")
                && !form.hasFlag("no_cursed_moon_target"))
            .toList();
        return starters.isEmpty() ? original
            : starters.get(player.getRandom().nextInt(starters.size())).id().toString();
    }

    public static String advanceRadiationForm(String base, int steps) {
        var form = FormRegistry.get(base);
        if (form == null || form.stage() <= 0) return base;
        for (int i = 0; i < steps; i++) {
            var next = FormRegistry.nextInProgression(form);
            if (next == null || next.stage() <= form.stage() || next.hasFlag("sub_form")
                || FormRegistry.masterFormOf(next.id()) != null) break;
            form = next;
        }
        return form.id().toString();
    }

    public static boolean applyRadiationForm(Player player, String id) {
        var target = ResourceLocation.tryParse(id);
        var data = SscApi.currentForm(player).orElse(null);
        if (target == null || data == null || FormRegistry.get(target) == null) return false;
        String previous = data.getPreviousFormId();
        boolean changed = FormManager.setForm(player, target, false);
        // Temporary shifts must not replace SSC's own previous-form history.
        data.setPreviousFormId(previous);
        return changed || id.equals(data.getFormId());
    }
}
