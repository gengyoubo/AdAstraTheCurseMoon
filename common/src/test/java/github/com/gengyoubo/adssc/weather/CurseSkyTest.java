package github.com.gengyoubo.adssc.weather;

import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

/** Dependency-free behavioral tests, run by :common:testCurseSky and the build's check task. */
public final class CurseSkyTest {
    private static final RandomGenerator ALWAYS = new Fixed(0);
    private static final RandomGenerator NEVER = new Fixed(1);
    private static int assertions;

    public static void main(String[] args) {
        equal(0.0, CurseSkyRules.stormChance(0), "no storms outside the system");
        equal(0.0, CurseSkyRules.flareChance(0), "no flares outside the system");
        for (int tier = 1; tier < 4; tier++) {
            check(CurseSkyRules.stormChance(tier + 1) > CurseSkyRules.stormChance(tier), "storm chance rises with tier " + tier);
            check(CurseSkyRules.flareChance(tier + 1) > CurseSkyRules.flareChance(tier), "flare chance rises with tier " + tier);
        }

        int previous = 0;
        for (int tier = 1; tier <= 4; tier++) {
            int stormy = stormyTicks(tier, 200 * 72000, new SplittableRandom(tier));
            check(stormy > previous, "tier " + tier + " is stormier than tier " + (tier - 1));
            previous = stormy;
        }

        var sky = new CurseSky();
        run(sky, CurseSkyRules.ROLL_INTERVAL - 1, ALWAYS);
        equal(0, sky.flags(), "nothing happens before the first roll");
        sky.tick(1, ALWAYS);
        equal(true, sky.storm(), "a successful roll starts a storm");
        equal(CurseSky.Flare.WARNING, sky.flare(), "a successful roll warns of a flare");
        equal(CurseSky.STORM | CurseSky.FLARE_WARNING, sky.flags(), "flags report storm and warning");
        equal(CurseSkyRules.FLARE_WARNING_TICKS, sky.warningTicks(), "warning counts down from its full length");
        run(sky, CurseSkyRules.FLARE_WARNING_TICKS - 1, NEVER);
        equal(CurseSky.Flare.WARNING, sky.flare(), "warning lasts its full length");
        sky.tick(1, NEVER);
        equal(CurseSky.Flare.ACTIVE, sky.flare(), "the flare erupts after the warning");
        equal(CurseSky.STORM | CurseSky.FLARE_ACTIVE, sky.flags(), "flags report the active flare");
        run(sky, CurseSkyRules.FLARE_ACTIVE_TICKS, NEVER);
        equal(CurseSky.Flare.NONE, sky.flare(), "the flare ends after its active phase");
        run(sky, CurseSkyRules.STORM_MIN_TICKS - CurseSkyRules.FLARE_WARNING_TICKS - CurseSkyRules.FLARE_ACTIVE_TICKS - 1, NEVER);
        equal(true, sky.storm(), "the shortest storm still lasts its minimum");
        sky.tick(1, NEVER);
        equal(false, sky.storm(), "the storm ends after its duration");

        run(sky, CurseSkyRules.FLARE_COOLDOWN_TICKS - CurseSkyRules.STORM_MIN_TICKS + CurseSkyRules.FLARE_WARNING_TICKS + CurseSkyRules.FLARE_ACTIVE_TICKS - 1, ALWAYS);
        equal(CurseSky.Flare.NONE, sky.flare(), "cooldown blocks a new flare");
        int wait = 0;
        while (sky.flare() == CurseSky.Flare.NONE && wait <= CurseSkyRules.ROLL_INTERVAL) {
            sky.tick(1, ALWAYS);
            wait++;
        }
        equal(CurseSky.Flare.WARNING, sky.flare(), "a flare follows at the first roll after the cooldown");
        check(wait <= CurseSkyRules.ROLL_INTERVAL, "the next roll comes within a minute");

        var loaded = new CurseSky();
        loaded.load(sky.snapshot());
        equal(sky.snapshot(), loaded.snapshot(), "saved state round-trips");
        loaded.load(new CurseSky.Snapshot(-1, -5, 99999, -3));
        equal(new CurseSky.Snapshot(CurseSkyRules.ROLL_INTERVAL - 1, 0,
            CurseSkyRules.FLARE_WARNING_TICKS + CurseSkyRules.FLARE_ACTIVE_TICKS, 0), loaded.snapshot(), "corrupt saves are clamped");

        for (int tier = 1; tier <= 4; tier++) {
            for (int stage = 0; stage < 4; stage++) {
                check(CurseSkyRules.stormDamage(tier, stage + 1) < CurseSkyRules.stormDamage(tier, stage), "higher stage resists the storm");
                check(CurseSkyRules.flareDamage(tier, stage + 1) < CurseSkyRules.flareDamage(tier, stage), "higher stage resists the flare");
            }
            check(CurseSkyRules.stormDamage(tier, 4) > 0, "the storm always hurts a little");
            if (tier < 4) {
                check(CurseSkyRules.stormDamage(tier + 1, 0) > CurseSkyRules.stormDamage(tier, 0), "storm damage rises with tier");
                check(CurseSkyRules.flareDamage(tier + 1, 2) > CurseSkyRules.flareDamage(tier, 2), "flare damage rises with tier");
            }
        }
        equal(1f, CurseSkyRules.stormDamage(1, 0), "outer satellite storm deals half a heart");
        equal(0f, CurseSkyRules.stormDamage(0, 0), "no storm damage outside the system");
        equal(CurseSkyRules.resistance(4), CurseSkyRules.resistance(9), "resistance is capped");
        equal(0f, CurseSkyRules.resistance(-1), "negative stages get no resistance");
        float exposedFlare = CurseSkyRules.flareDamage(1, 0)
            * (CurseSkyRules.FLARE_ACTIVE_TICKS / CurseSkyRules.FLARE_DAMAGE_INTERVAL);
        check(exposedFlare > 20, "a full flare kills an exposed human on the outer satellite");

        equal(true, CurseSkyRules.stormExposed(true, 0), "an open roof exposes");
        equal(false, CurseSkyRules.stormExposed(false, CurseSkyRules.EXPOSED_SIDES - 1), "a doorway still shelters");
        equal(true, CurseSkyRules.stormExposed(false, CurseSkyRules.EXPOSED_SIDES), "an open pavilion exposes");

        equal(CurseSkyRules.METEOR_DAMAGE, CurseSkyRules.meteorDamage(0), "direct hits deal full damage");
        equal(0f, CurseSkyRules.meteorDamage(CurseSkyRules.METEOR_RADIUS), "no damage at the edge");
        check(CurseSkyRules.meteorDamage(1) > CurseSkyRules.meteorDamage(2), "damage falls off with distance");

        System.out.println("Curse sky: " + assertions + " assertions passed.");
    }

    private static int stormyTicks(int tier, int ticks, RandomGenerator random) {
        var sky = new CurseSky();
        int stormy = 0;
        for (int i = 0; i < ticks; i++) {
            sky.tick(tier, random);
            if (sky.storm()) stormy++;
        }
        return stormy;
    }

    private static void run(CurseSky sky, int ticks, RandomGenerator random) {
        for (int i = 0; i < ticks; i++) sky.tick(1, random);
    }

    private static void check(boolean condition, String message) {
        equal(true, condition, message);
    }

    private static void equal(Object expected, Object actual, String message) {
        assertions++;
        if (!expected.equals(actual)) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }

    /** Rolls a constant, so chances either always or never succeed and durations are their minimum. */
    private record Fixed(double value) implements RandomGenerator {
        public long nextLong() {
            return 0;
        }

        @Override
        public double nextDouble() {
            return value;
        }

        @Override
        public int nextInt(int origin, int bound) {
            return origin;
        }
    }
}
