package github.com.gengyoubo.adssc.radiation;

/** Dependency-free behavioral tests, run by :common:testRadiation and the build's check task. */
public final class RadiationExposureTest {
    private static int assertions;

    public static void main(String[] args) {
        for (int stage = 1; stage <= 4; stage++) {
            for (int level = 1; level <= 4; level++) {
                var forms = new FakeForms("wolf" + stage, true);
                var exposure = new RadiationExposure();
                equal(0, run(exposure, forms, level, 19), "no damage before a full second");
                equal("wolf" + Math.min(4, stage + level), forms.id, "temporary progression capped at endpoint");
                equal(Math.max(0, level - stage), exposure.tick(level, forms), "damage uses original stage");
                equal(5 * Math.max(0, level - stage), run(exposure, forms, level, 100), "steady damage per second");
                exposure.tick(0, forms);
                equal("wolf" + stage, forms.id, "leaving restores original form");
                equal(false, exposure.active(), "leaving clears state");
            }
        }

        var forms = new FakeForms("wolf1", true);
        var exposure = new RadiationExposure();
        run(exposure, forms, 2, 10);
        equal("wolf3", forms.id, "stage 1 plus radiation 2 is stage 3");
        equal(3, run(exposure, forms, 4, 10), "changing planet preserves damage cadence");
        equal("wolf4", forms.id, "increased radiation caps at endpoint");
        exposure.tick(1, forms);
        equal("wolf2", forms.id, "lower radiation recomputes from original, without stacking");
        var loaded = new RadiationExposure();
        loaded.load(exposure.snapshot());
        loaded.tick(1, forms);
        equal("wolf2", forms.id, "reconnect does not add stages again");
        loaded.tick(0, forms);
        equal("wolf1", forms.id, "reloaded state restores original");

        forms = new FakeForms("human", false);
        exposure = new RadiationExposure();
        equal(4, run(exposure, forms, 4, 20), "disabled human takes full radiation damage");
        equal("human", forms.id, "disabled SSC is not enabled by radiation");
        equal(0, forms.randomSelections, "disabled human never randomizes");

        forms = new FakeForms("human", true);
        exposure = new RadiationExposure();
        equal(2, run(exposure, forms, 2, 20), "enabled human still uses original stage zero for damage");
        equal("wolf2", forms.id, "random starter consumes first progression step");
        equal(1, forms.randomSelections, "one random choice per exposure");
        exposure.tick(4, forms);
        equal(1, forms.randomSelections, "travel preserves the random lineage");
        exposure.tick(0, forms);
        equal("human", forms.id, "random temporary form restores to enabled human");

        forms = new FakeForms("wolf1", true);
        forms.maximumStage = 1;
        exposure = new RadiationExposure();
        equal(2, run(exposure, forms, 3, 20), "no successor still takes stage-difference damage");
        equal("wolf1", forms.id, "terminal main node is unchanged");

        forms = new FakeForms("wolf1", true);
        exposure = new RadiationExposure();
        exposure.tick(2, forms);
        forms.id = "fox2";
        exposure.tick(2, forms);
        equal("fox4", forms.id, "external permanent transformation becomes the new baseline");
        exposure.tick(0, forms);
        equal("fox2", forms.id, "leaving preserves external permanent transformation");

        exposure.tick(1, forms);
        forms.id = "wolf4";
        exposure.tick(0, forms);
        equal("wolf4", forms.id, "external change at exit is not overwritten");

        forms = new FakeForms("wolf1", true);
        exposure = new RadiationExposure();
        forms.allowApply = false;
        exposure.tick(2, forms);
        equal("wolf1", forms.id, "failed transformation does not invent a temporary form");
        forms.allowApply = true;
        exposure.tick(2, forms);
        equal("wolf3", forms.id, "failed transformation can retry");
        forms.allowApply = false;
        exposure.tick(0, forms);
        equal(true, exposure.active(), "failed restore keeps recovery state");
        forms.allowApply = true;
        exposure.tick(0, forms);
        equal("wolf1", forms.id, "restore retries successfully");
        equal(0, run(exposure, forms, 0, 100), "no damage outside radiation dimensions");

        System.out.println("Radiation exposure: " + assertions + " assertions passed.");
    }

    private static int run(RadiationExposure state, FakeForms forms, int level, int ticks) {
        int damage = 0;
        for (int i = 0; i < ticks; i++) damage += state.tick(level, forms);
        return damage;
    }

    private static void equal(Object expected, Object actual, String message) {
        assertions++;
        if (!expected.equals(actual)) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }

    private static final class FakeForms implements RadiationExposure.Forms {
        String id;
        final boolean enabled;
        boolean allowApply = true;
        int maximumStage = 4;
        int randomSelections;

        FakeForms(String id, boolean enabled) {
            this.id = id;
            this.enabled = enabled;
        }

        public RadiationExposure.Form current() {
            return new RadiationExposure.Form(id, id.equals("human") ? 0 : Character.getNumericValue(id.charAt(id.length() - 1)), enabled);
        }

        public String progressionBase(RadiationExposure.Form original) {
            if (!original.id().equals("human")) return original.id();
            randomSelections++;
            return "wolf1";
        }

        public String advance(String base, int steps) {
            if (base.equals("human")) return base;
            int stage = Character.getNumericValue(base.charAt(base.length() - 1));
            return base.substring(0, base.length() - 1) + Math.min(maximumStage, stage + steps);
        }

        public boolean apply(String target) {
            if (!allowApply) return false;
            id = target;
            return true;
        }
    }
}
