package github.com.gengyoubo.adssc.weather;

/** Chances, timings and damage formulas for the Curse Moon system's storms and flares. */
public final class CurseSkyRules {
    /** Storms and flares are rolled once a minute. */
    public static final int ROLL_INTERVAL = 1200;
    public static final int STORM_MIN_TICKS = 2400;
    public static final int STORM_MAX_TICKS = 6000;
    public static final int STORM_DAMAGE_INTERVAL = 40;
    public static final int FLARE_WARNING_TICKS = 200;
    public static final int FLARE_ACTIVE_TICKS = 200;
    public static final int FLARE_COOLDOWN_TICKS = 6000;
    public static final int FLARE_DAMAGE_INTERVAL = 20;
    public static final int METEOR_INTERVAL = 10;
    public static final double METEOR_RADIUS = 3.0;
    public static final float METEOR_DAMAGE = 10f;
    /** Blocks checked in each horizontal direction for storm shelter. */
    public static final int SHELTER_REACH = 8;
    /** Of the eight horizontal directions, this many open ones expose a roofed player to the storm. */
    public static final int EXPOSED_SIDES = 4;

    private CurseSkyRules() {}

    public static double stormChance(int tier) {
        return tier <= 0 ? 0 : 0.04 * Math.min(4, tier);
    }

    public static double flareChance(int tier) {
        return tier <= 0 ? 0 : 0.01 + 0.01 * Math.min(4, tier);
    }

    /** Fraction of storm and flare damage an SSC form of this stage ignores. */
    public static float resistance(int stage) {
        return Math.max(0f, Math.min(0.8f, 0.2f * stage));
    }

    public static float stormDamage(int tier, int stage) {
        if (tier <= 0) return 0f;
        return (1f + 0.5f * (Math.min(4, tier) - 1)) * (1f - resistance(stage));
    }

    public static float flareDamage(int tier, int stage) {
        if (tier <= 0) return 0f;
        return 3f * Math.min(4, tier) * (1f - resistance(stage));
    }

    public static boolean stormExposed(boolean roofOpen, int openSides) {
        return roofOpen || openSides >= EXPOSED_SIDES;
    }

    /** Falls off linearly from the impact point to nothing at {@link #METEOR_RADIUS}. */
    public static float meteorDamage(double distance) {
        if (distance >= METEOR_RADIUS) return 0f;
        return (float) (METEOR_DAMAGE * (1 - Math.max(0, distance) / METEOR_RADIUS));
    }
}
