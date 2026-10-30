package github.com.gengyoubo.adssc.weather;

import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.compat.SscCompat;
import github.com.gengyoubo.adssc.radiation.RadiationDataHolder;
import github.com.gengyoubo.adssc.registry.ModDimensions;
import io.netty.buffer.Unpooled;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Moon dust storms and Curse Moon flares on the system's four surfaces. */
public final class CurseSkyWeather {
    public static final ResourceLocation PACKET = id("curse_sky");
    private static final ResourceKey<DamageType> STORM = damage("moon_dust_storm");
    private static final ResourceKey<DamageType> FLARE = damage("curse_moon_flare");
    private static final ResourceKey<DamageType> METEOR = damage("curse_moon_meteor");
    private static final ResourceLocation ROCK = new ResourceLocation("ad_astra", "moon_cobblestone");
    private static final int MAX_METEORS = 128;
    private static final double METEOR_SPREAD = 24;
    private static final int METEOR_HEIGHT = 40;

    private record Sent(ResourceKey<Level> dimension, int flags) {}

    private static final Map<UUID, Sent> SENT = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<FallingBlockEntity>> METEORS = new HashMap<>();

    private CurseSkyWeather() {}

    public static void init() {
        TickEvent.SERVER_LEVEL_POST.register(CurseSkyWeather::tick);
        PlayerEvent.PLAYER_QUIT.register(player -> SENT.remove(player.getUUID()));
        LifecycleEvent.SERVER_STOPPED.register(server -> {
            SENT.clear();
            METEORS.clear();
        });
    }

    private static void tick(ServerLevel level) {
        int tier = ModDimensions.surfaceTier(level.dimension());
        if (tier == 0) {
            for (var player : level.players()) sync(player, 0);
            return;
        }

        var data = CurseSkyData.get(level);
        var sky = data.sky();
        boolean wasStorm = sky.storm();
        var wasFlare = sky.flare();
        sky.tick(tier, level.random::nextLong);
        data.setDirty();
        boolean eruption = ModDimensions.CURSE_MOON.equals(level.dimension());
        announce(level, sky, wasStorm, wasFlare, eruption);
        tickMeteors(level);

        long time = level.getGameTime();
        for (var player : level.players()) {
            sync(player, sky.flags());
            if (!player.isAlive() || player.isSpectator()) continue;
            if (sky.storm() && time % CurseSkyRules.STORM_DAMAGE_INTERVAL == 0 && stormExposed(level, player)) {
                hurt(player, STORM, CurseSkyRules.stormDamage(tier, formStage(player)));
            }
            if (sky.flare() != CurseSky.Flare.ACTIVE) continue;
            if (eruption) {
                if (time % CurseSkyRules.METEOR_INTERVAL == 0) spawnMeteor(level, player);
            } else if (time % CurseSkyRules.FLARE_DAMAGE_INTERVAL == 0 && roofOpen(level, player)) {
                hurt(player, FLARE, CurseSkyRules.flareDamage(tier, formStage(player)));
            }
        }
    }

    private static void announce(ServerLevel level, CurseSky sky, boolean wasStorm, CurseSky.Flare wasFlare, boolean eruption) {
        if (sky.storm() != wasStorm) {
            tell(level, sky.storm() ? "message.ad_ssc.moon_dust_storm.start" : "message.ad_ssc.moon_dust_storm.end");
        }
        var flare = sky.flare();
        if (flare == CurseSky.Flare.WARNING && sky.warningTicks() % 20 == 0) {
            tell(level, eruption ? "message.ad_ssc.curse_moon_flare.eruption_warning" : "message.ad_ssc.curse_moon_flare.warning",
                sky.warningTicks() / 20);
        } else if (flare != wasFlare) {
            if (flare == CurseSky.Flare.ACTIVE) {
                tell(level, eruption ? "message.ad_ssc.curse_moon_flare.eruption" : "message.ad_ssc.curse_moon_flare.start");
            } else if (flare == CurseSky.Flare.NONE) {
                tell(level, "message.ad_ssc.curse_moon_flare.end");
            }
        }
    }

    private static void tell(ServerLevel level, String key, Object... args) {
        var message = Component.translatable(key, args).withStyle(ChatFormatting.LIGHT_PURPLE);
        for (var player : level.players()) player.displayClientMessage(message, true);
    }

    private static void sync(ServerPlayer player, int flags) {
        var sent = new Sent(player.level().dimension(), flags);
        if (sent.equals(SENT.put(player.getUUID(), sent))) return;
        var buf = new FriendlyByteBuf(Unpooled.buffer());
        buf.writeResourceLocation(sent.dimension().location());
        buf.writeByte(flags);
        NetworkManager.sendToPlayer(player, PACKET, buf);
    }

    /** Resistance uses the form the player had before radiation advanced it, like radiation damage. */
    private static int formStage(ServerPlayer player) {
        var exposure = ((RadiationDataHolder) player).adssc$radiation();
        return exposure.active() ? exposure.originalStage() : SscCompat.radiationForm(player).stage();
    }

    private static boolean roofOpen(ServerLevel level, ServerPlayer player) {
        var eye = BlockPos.containing(player.getEyePosition());
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, eye.getX(), eye.getZ()) <= eye.getY();
    }

    private static boolean stormExposed(ServerLevel level, ServerPlayer player) {
        if (roofOpen(level, player)) return true;
        var eye = BlockPos.containing(player.getEyePosition());
        var pos = new BlockPos.MutableBlockPos();
        int open = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                boolean blocked = false;
                for (int i = 1; i <= CurseSkyRules.SHELTER_REACH && !blocked; i++) {
                    pos.set(eye.getX() + dx * i, eye.getY(), eye.getZ() + dz * i);
                    blocked = !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
                }
                if (!blocked) open++;
            }
        }
        return CurseSkyRules.stormExposed(false, open);
    }

    private static void spawnMeteor(ServerLevel level, ServerPlayer player) {
        var meteors = METEORS.computeIfAbsent(level.dimension(), key -> new ArrayList<>());
        if (meteors.size() >= MAX_METEORS) return;
        var random = level.random;
        double angle = random.nextDouble() * Mth.TWO_PI;
        double distance = Math.sqrt(random.nextDouble()) * METEOR_SPREAD;
        int x = Mth.floor(player.getX() + Math.cos(angle) * distance);
        int z = Mth.floor(player.getZ() + Math.sin(angle) * distance);
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
        int y = Math.min(level.getMaxBuildHeight() - 2, Math.max(surface, player.getBlockY()) + METEOR_HEIGHT);
        var pos = new BlockPos(x, y, z);
        if (y <= surface || !level.isLoaded(pos) || !level.isEmptyBlock(pos)) return;

        var rock = FallingBlockEntity.fall(level, pos, rock());
        rock.dropItem = false;
        rock.setDeltaMovement(random.nextGaussian() * 0.1, -1.2, random.nextGaussian() * 0.1);
        meteors.add(rock);
    }

    private static BlockState rock() {
        return BuiltInRegistries.BLOCK.getOptional(ROCK).orElse(Blocks.COBBLESTONE).defaultBlockState();
    }

    private static void tickMeteors(ServerLevel level) {
        var meteors = METEORS.get(level.dimension());
        if (meteors == null) return;
        meteors.removeIf(rock -> {
            if (!rock.isRemoved()) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE, rock.getX(), rock.getY() + 0.5, rock.getZ(), 2, 0.2, 0.2, 0.2, 0.01);
                return false;
            }
            // Landing discards the entity; chunk unloads must not detonate it.
            if (rock.getRemovalReason() == Entity.RemovalReason.DISCARDED) impact(level, rock.position());
            return true;
        });
    }

    private static void impact(ServerLevel level, Vec3 center) {
        level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.5, center.z, 1, 0, 0, 0, 0);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.WEATHER, 1.5f, 1.3f);
        var area = new AABB(center, center).inflate(CurseSkyRules.METEOR_RADIUS);
        for (var entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity.isSpectator()) continue;
            hurt(entity, METEOR, CurseSkyRules.meteorDamage(entity.position().distanceTo(center)));
        }
    }

    private static void hurt(LivingEntity entity, ResourceKey<DamageType> key, float amount) {
        if (amount <= 0) return;
        var type = entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
        entity.hurt(new DamageSource(type), amount);
    }

    private static ResourceKey<DamageType> damage(String path) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, id(path));
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, path);
    }
}
