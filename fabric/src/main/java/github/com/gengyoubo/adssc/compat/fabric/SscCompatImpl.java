package github.com.gengyoubo.adssc.compat.fabric;

import github.com.gengyoubo.adssc.radiation.RadiationExposure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.onixary.shapeShifterCurseFabric.player_form.RegPlayerForms;
import net.onixary.shapeShifterCurseFabric.player_form.PlayerFormPhase;
import net.onixary.shapeShifterCurseFabric.player_form.ability.FormAbilityManager;
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

    public static RadiationExposure.Form radiationForm(Player player) {
        return RegPlayerFormComponent.PLAYER_FORM.maybeGet(player).map(component -> {
            var form = component.getCurrentForm();
            boolean enabled = !form.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE);
            int stage = !enabled || form.equals(RegPlayerForms.ORIGINAL_SHIFTER) ? 0
                : form.getPhase() == PlayerFormPhase.PHASE_SP ? 1 : Math.max(1, form.getIndex() + 1);
            return new RadiationExposure.Form(form.getIDString(), stage, enabled);
        }).orElse(new RadiationExposure.Form(RegPlayerForms.ORIGINAL_BEFORE_ENABLE.getIDString(), 0, false));
    }

    public static String radiationBase(Player player, String original) {
        if (!RegPlayerForms.ORIGINAL_SHIFTER.getIDString().equals(original)) return original;
        var starters = RegPlayerForms.playerForms.values().stream()
            .filter(form -> form.getPhase() == PlayerFormPhase.PHASE_0 && !form.getIgnoreCursedMoon())
            .toList();
        return starters.isEmpty() ? original
            : starters.get(player.getRandom().nextInt(starters.size())).getIDString();
    }

    public static String advanceRadiationForm(String base, int steps) {
        var form = RegPlayerForms.getPlayerForm(base);
        if (form == null || form.equals(RegPlayerForms.ORIGINAL_BEFORE_ENABLE)
            || form.equals(RegPlayerForms.ORIGINAL_SHIFTER)) return base;
        for (int i = 0; i < steps; i++) {
            var group = form.getGroup();
            if (group == null) break;
            var next = group.getNextForm(form);
            if (next == null || next.getIndex() <= form.getIndex()) break;
            form = next;
        }
        return form.getIDString();
    }

    public static boolean applyRadiationForm(Player player, String id) {
        var target = RegPlayerForms.getPlayerForm(id);
        var component = RegPlayerFormComponent.PLAYER_FORM.maybeGet(player).orElse(null);
        if (target == null || component == null) return false;
        var before = new CompoundTag();
        component.writeToNbt(before);
        FormAbilityManager.applyForm(player, target);
        var after = new CompoundTag();
        component.writeToNbt(after);
        after.putString("previousForm", before.getString("previousForm"));
        component.readFromNbt(after);
        RegPlayerFormComponent.PLAYER_FORM.sync(player);
        return id.equals(component.getCurrentForm().getIDString());
    }
}
