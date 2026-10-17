package github.com.gengyoubo.adssc.registry;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/** Dimension and planet definitions are loaded from the shared datapack resources. */
public final class ModDimensions {
    public static final ResourceLocation CURSE_MOON_SYSTEM = id("curse_moon");
    public static final ResourceKey<Level> CURSE_MOON = ResourceKey.create(Registries.DIMENSION, id("curse_moon"));
    public static final ResourceKey<Level> CURSE_MOON_ORBIT = ResourceKey.create(Registries.DIMENSION, id("curse_moon_orbit"));

    private ModDimensions() {
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, path);
    }
}
