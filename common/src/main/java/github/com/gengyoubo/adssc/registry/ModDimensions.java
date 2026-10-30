package github.com.gengyoubo.adssc.registry;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Set;

/** Dimension and planet definitions are loaded from the shared datapack resources. */
public final class ModDimensions {
    public static final ResourceLocation CURSE_MOON_SYSTEM = id("curse_moon");
    public static final ResourceKey<Level> CURSE_MOON = ResourceKey.create(Registries.DIMENSION, id("curse_moon"));
    public static final ResourceKey<Level> CURSE_MOON_ORBIT = ResourceKey.create(Registries.DIMENSION, id("curse_moon_orbit"));
    public static final ResourceKey<Level> CURSE_SATELLITE_OUTER = ResourceKey.create(Registries.DIMENSION, id("curse_satellite_outer"));
    public static final ResourceKey<Level> CURSE_SATELLITE_OUTER_ORBIT = ResourceKey.create(Registries.DIMENSION, id("curse_satellite_outer_orbit"));
    public static final ResourceKey<Level> CURSE_SATELLITE_MIDDLE = ResourceKey.create(Registries.DIMENSION, id("curse_satellite_middle"));
    public static final ResourceKey<Level> CURSE_SATELLITE_MIDDLE_ORBIT = ResourceKey.create(Registries.DIMENSION, id("curse_satellite_middle_orbit"));
    public static final ResourceKey<Level> CURSE_SATELLITE_INNER = ResourceKey.create(Registries.DIMENSION, id("curse_satellite_inner"));
    public static final ResourceKey<Level> CURSE_SATELLITE_INNER_ORBIT = ResourceKey.create(Registries.DIMENSION, id("curse_satellite_inner_orbit"));

    private static final Set<ResourceKey<Level>> CURSE_MOON_DIMENSIONS = Set.of(
        CURSE_MOON, CURSE_MOON_ORBIT,
        CURSE_SATELLITE_OUTER, CURSE_SATELLITE_OUTER_ORBIT,
        CURSE_SATELLITE_MIDDLE, CURSE_SATELLITE_MIDDLE_ORBIT,
        CURSE_SATELLITE_INNER, CURSE_SATELLITE_INNER_ORBIT
    );

    private ModDimensions() {
    }

    public static boolean isCurseMoonSystem(ResourceKey<Level> dimension) {
        return CURSE_MOON_DIMENSIONS.contains(dimension);
    }

    public static int radiationLevel(ResourceKey<Level> dimension) {
        if (CURSE_SATELLITE_OUTER.equals(dimension) || CURSE_SATELLITE_OUTER_ORBIT.equals(dimension)) return 1;
        if (CURSE_SATELLITE_MIDDLE.equals(dimension) || CURSE_SATELLITE_MIDDLE_ORBIT.equals(dimension)) return 2;
        if (CURSE_SATELLITE_INNER.equals(dimension) || CURSE_SATELLITE_INNER_ORBIT.equals(dimension)) return 3;
        if (CURSE_MOON.equals(dimension) || CURSE_MOON_ORBIT.equals(dimension)) return 4;
        return 0;
    }

    /** The planet tier of a landable surface in the system; orbits and other dimensions are 0. */
    public static int surfaceTier(ResourceKey<Level> dimension) {
        if (CURSE_SATELLITE_OUTER.equals(dimension)) return 1;
        if (CURSE_SATELLITE_MIDDLE.equals(dimension)) return 2;
        if (CURSE_SATELLITE_INNER.equals(dimension)) return 3;
        if (CURSE_MOON.equals(dimension)) return 4;
        return 0;
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, path);
    }
}
