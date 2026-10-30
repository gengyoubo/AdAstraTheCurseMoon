package github.com.gengyoubo.adssc.client;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.architectury.event.events.client.ClientPlayerEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.networking.NetworkManager;
import github.com.gengyoubo.adssc.weather.CurseSky;
import github.com.gengyoubo.adssc.weather.CurseSkyWeather;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FogType;
import org.joml.Vector3f;

/** Purple moon dust fog and particles for storms, and a fog flash for flares. */
public final class CurseSkyClient {
    private static final float[] DUST_FOG = {0.36f, 0.23f, 0.54f};
    private static final float[] FLARE_FOG = {0.85f, 0.38f, 0.82f};
    private static final DustParticleOptions DUST = new DustParticleOptions(new Vector3f(0.62f, 0.42f, 0.86f), 1.6f);
    private static final DustParticleOptions DARK_DUST = new DustParticleOptions(new Vector3f(0.40f, 0.25f, 0.60f), 2.4f);
    private static final double PARTICLE_RANGE = 20;
    private static final float WIND_SPEED = 0.6f;

    private static ResourceKey<Level> dimension;
    private static int flags;
    private static float storm;
    private static float stormO;
    private static float flare;
    private static float flareO;

    private CurseSkyClient() {}

    public static void init() {
        NetworkManager.registerReceiver(NetworkManager.s2c(), CurseSkyWeather.PACKET, (buf, context) -> {
            var key = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
            int received = buf.readByte();
            context.queue(() -> {
                dimension = key;
                flags = received;
            });
        });
        ClientTickEvent.CLIENT_POST.register(CurseSkyClient::tick);
        ClientPlayerEvent.CLIENT_PLAYER_QUIT.register(player -> {
            dimension = null;
            flags = 0;
            storm = stormO = flare = flareO = 0;
        });
    }

    private static void tick(Minecraft minecraft) {
        var level = minecraft.level;
        if (level == null || minecraft.isPaused()) return;
        boolean here = level.dimension().equals(dimension);
        stormO = storm;
        storm = approach(storm, here && (flags & CurseSky.STORM) != 0, 0.01f);
        flareO = flare;
        flare = approach(flare, here && (flags & CurseSky.FLARE_ACTIVE) != 0, 0.05f);
        if (storm > 0) spawnDust(minecraft);
    }

    private static float approach(float value, boolean on, float step) {
        return Mth.clamp(value + (on ? step : -step), 0f, 1f);
    }

    private static void spawnDust(Minecraft minecraft) {
        var level = minecraft.level;
        int count = Math.round(storm * switch (minecraft.options.particles().get()) {
            case ALL -> 24;
            case DECREASED -> 10;
            case MINIMAL -> 3;
        });
        var camera = minecraft.gameRenderer.getMainCamera().getPosition();
        var random = level.random;
        // The wind slowly veers over a few minutes.
        double wind = level.getGameTime() / 2400.0 * Mth.TWO_PI;
        double windX = Math.cos(wind) * WIND_SPEED;
        double windZ = Math.sin(wind) * WIND_SPEED;
        for (int i = 0; i < count; i++) {
            double x = camera.x + (random.nextDouble() * 2 - 1) * PARTICLE_RANGE;
            double y = camera.y + random.nextDouble() * 16 - 6;
            double z = camera.z + (random.nextDouble() * 2 - 1) * PARTICLE_RANGE;
            // Only blow over open ground, never through roofs or caves.
            if (y < level.getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z))) continue;
            var particle = minecraft.particleEngine.createParticle(i % 3 == 0 ? DARK_DUST : DUST, x, y, z, 0, 0, 0);
            if (particle == null) continue;
            particle.setParticleSpeed(windX + random.nextGaussian() * 0.05, random.nextGaussian() * 0.02,
                windZ + random.nextGaussian() * 0.05);
            particle.setLifetime(40 + random.nextInt(30));
        }
    }

    /** Called after vanilla picks the fog color; returns the new color, or null to keep it. */
    public static float[] fogColor(Camera camera, float partialTick, float red, float green, float blue) {
        float dust = Mth.lerp(partialTick, stormO, storm);
        float burst = Mth.lerp(partialTick, flareO, flare);
        if (dust <= 0 && burst <= 0 || camera.getFluidInCamera() != FogType.NONE) return null;
        float[] color = {red, green, blue};
        for (int i = 0; i < 3; i++) {
            color[i] = Mth.lerp(dust, color[i], DUST_FOG[i]);
            // Flares pulse about twice a second.
            float pulse = 0.35f + 0.25f * Mth.sin((camera.getEntity().tickCount + partialTick) * 0.6f);
            color[i] = Mth.lerp(burst * pulse, color[i], FLARE_FOG[i]);
        }
        return color;
    }

    /** Thickens the fog during storms; never thins fog that vanilla or another mod already set. */
    public static void setupFog(Camera camera, FogRenderer.FogMode mode, float partialTick) {
        float dust = Mth.lerp(partialTick, stormO, storm);
        if (dust <= 0 || camera.getFluidInCamera() != FogType.NONE) return;
        boolean sky = mode == FogRenderer.FogMode.FOG_SKY;
        float start = Mth.lerp(dust, RenderSystem.getShaderFogStart(), sky ? 0 : 2);
        float end = Mth.lerp(dust, RenderSystem.getShaderFogEnd(), sky ? 12 : 28);
        RenderSystem.setShaderFogStart(Math.min(start, RenderSystem.getShaderFogStart()));
        RenderSystem.setShaderFogEnd(Math.min(end, RenderSystem.getShaderFogEnd()));
    }
}
