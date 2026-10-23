package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public final class ModEffects {
    private static final DeferredRegister<MobEffect> EFFECTS =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.MOB_EFFECT);

    public static final RegistrySupplier<MobEffect> CURSE_MOON_RADIATION = EFFECTS.register(
        "curse_moon_radiation", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xb667e6) {});

    private ModEffects() {}

    public static void register() {
        EFFECTS.register();
    }
}
