package github.com.gengyoubo.adssc.radiation;

import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import github.com.gengyoubo.adssc.compat.SscCompat;
import github.com.gengyoubo.adssc.registry.ModDimensions;
import github.com.gengyoubo.adssc.registry.ModEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;

public final class CurseMoonRadiation {
    private static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(
        Registries.DAMAGE_TYPE, new ResourceLocation("ad_ssc", "curse_moon_radiation"));

    private CurseMoonRadiation() {}

    public static void init() {
        TickEvent.PLAYER_POST.register(player -> {
            if (player instanceof ServerPlayer serverPlayer && player.isAlive()) tick(serverPlayer);
        });
        PlayerEvent.PLAYER_CLONE.register((oldPlayer, newPlayer, wonGame) ->
            exposure(newPlayer).load(exposure(oldPlayer).snapshot()));
    }

    private static RadiationExposure exposure(ServerPlayer player) {
        return ((RadiationDataHolder) player).adssc$radiation();
    }

    private static void tick(ServerPlayer player) {
        int level = player.isSpectator() ? 0 : ModDimensions.radiationLevel(player.level().dimension());
        var effect = ModEffects.CURSE_MOON_RADIATION.get();
        if (level == 0) {
            player.removeEffect(effect);
        } else {
            var instance = player.getEffect(effect);
            if (instance == null || instance.getAmplifier() != level - 1 || !instance.isInfiniteDuration()) {
                player.removeEffect(effect);
                player.addEffect(new MobEffectInstance(effect, -1, level - 1, false, false, true));
            }
        }

        var state = exposure(player);
        if (level == 0 && !state.active()) return;
        int damage = state.tick(level, new RadiationExposure.Forms() {
            public RadiationExposure.Form current() {
                return SscCompat.radiationForm(player);
            }

            public String progressionBase(RadiationExposure.Form original) {
                return SscCompat.radiationBase(player, original.id());
            }

            public String advance(String base, int steps) {
                return SscCompat.advanceRadiationForm(base, steps);
            }

            public boolean apply(String id) {
                float health = player.getHealth();
                boolean changed = SscCompat.applyRadiationForm(player, id);
                if (changed) player.setHealth(Math.min(health, player.getMaxHealth()));
                return changed;
            }
        });
        if (damage > 0) {
            var type = player.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE);
            player.hurt(new DamageSource(type), damage);
        }
    }
}
