package github.com.gengyoubo.adssc.radiation;

/** Server-side exposure state, independent of Minecraft and either SSC implementation. */
public final class RadiationExposure {
    public record Form(String id, int stage, boolean enabled) {}

    public interface Forms {
        Form current();
        String progressionBase(Form original);
        String advance(String base, int steps);
        boolean apply(String id);
    }

    public record Snapshot(String original, String base, String applied, int stage, int level, int ticks) {}

    private String original = "";
    private String base = "";
    private String applied = "";
    private int stage;
    private int level = -1;
    private int ticks;

    public boolean active() {
        return !original.isEmpty();
    }

    /** The stage before radiation advanced the form; 0 while inactive. */
    public int originalStage() {
        return stage;
    }

    /** Returns the damage due this tick; changing radiation level never resets the second. */
    public int tick(int radiationLevel, Forms forms) {
        Form current = forms.current();
        if (radiationLevel <= 0) {
            if (!active()) return 0;
            // An unrelated permanent transformation takes precedence over our temporary form.
            if (!current.id().equals(applied) || current.id().equals(original) || forms.apply(original)) {
                clear();
            }
            return 0;
        }

        if (!active() || !current.id().equals(applied)) {
            original = current.id();
            stage = Math.max(0, current.stage());
            base = current.enabled() ? forms.progressionBase(current) : original;
            applied = current.id();
            level = -1;
        }

        if (level != radiationLevel) {
            // For an enabled human, selecting a starter consumes the first progression step.
            int steps = radiationLevel - (stage == 0 && !base.equals(original) ? 1 : 0);
            String target = forms.advance(base, Math.max(0, steps));
            if (target.equals(current.id()) || forms.apply(target)) {
                applied = target;
                level = radiationLevel;
            }
        }

        if (++ticks < 20) return 0;
        ticks = 0;
        return Math.max(0, radiationLevel - stage);
    }

    public Snapshot snapshot() {
        return new Snapshot(original, base, applied, stage, level, ticks);
    }

    public void load(Snapshot saved) {
        original = saved.original();
        base = saved.base();
        applied = saved.applied();
        stage = Math.max(0, saved.stage());
        level = saved.level();
        ticks = Math.floorMod(saved.ticks(), 20);
    }

    private void clear() {
        original = base = applied = "";
        stage = ticks = 0;
        level = -1;
    }
}
