package github.com.gengyoubo.adssc.weather;

import java.util.random.RandomGenerator;

/** One dimension's storm and flare clock, independent of Minecraft. */
public final class CurseSky {
    public static final int STORM = 1;
    public static final int FLARE_WARNING = 2;
    public static final int FLARE_ACTIVE = 4;

    public enum Flare { NONE, WARNING, ACTIVE }

    public record Snapshot(int roll, int storm, int flare, int cooldown) {}

    private int roll;
    private int storm;
    private int flare;
    private int cooldown;

    public void tick(int tier, RandomGenerator random) {
        if (storm > 0) storm--;
        if (flare > 0) {
            if (--flare == 0) cooldown = CurseSkyRules.FLARE_COOLDOWN_TICKS;
        } else if (cooldown > 0) {
            cooldown--;
        }

        if (++roll < CurseSkyRules.ROLL_INTERVAL) return;
        roll = 0;
        if (storm == 0 && random.nextDouble() < CurseSkyRules.stormChance(tier)) {
            storm = random.nextInt(CurseSkyRules.STORM_MIN_TICKS, CurseSkyRules.STORM_MAX_TICKS + 1);
        }
        if (flare == 0 && cooldown == 0 && random.nextDouble() < CurseSkyRules.flareChance(tier)) {
            flare = CurseSkyRules.FLARE_WARNING_TICKS + CurseSkyRules.FLARE_ACTIVE_TICKS;
        }
    }

    public boolean storm() {
        return storm > 0;
    }

    public Flare flare() {
        if (flare == 0) return Flare.NONE;
        return flare > CurseSkyRules.FLARE_ACTIVE_TICKS ? Flare.WARNING : Flare.ACTIVE;
    }

    /** Ticks left before a warned flare erupts. */
    public int warningTicks() {
        return Math.max(0, flare - CurseSkyRules.FLARE_ACTIVE_TICKS);
    }

    /** The state sent to clients in this dimension. */
    public int flags() {
        return (storm() ? STORM : 0) | switch (flare()) {
            case NONE -> 0;
            case WARNING -> FLARE_WARNING;
            case ACTIVE -> FLARE_ACTIVE;
        };
    }

    public Snapshot snapshot() {
        return new Snapshot(roll, storm, flare, cooldown);
    }

    public void load(Snapshot saved) {
        roll = Math.floorMod(saved.roll(), CurseSkyRules.ROLL_INTERVAL);
        storm = Math.max(0, saved.storm());
        flare = Math.max(0, Math.min(saved.flare(), CurseSkyRules.FLARE_WARNING_TICKS + CurseSkyRules.FLARE_ACTIVE_TICKS));
        cooldown = Math.max(0, saved.cooldown());
    }
}
