package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.block.MoonDustSmeltingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class ModRecipes {
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.RECIPE_SERIALIZER);

    public static final RegistrySupplier<RecipeSerializer<MoonDustSmeltingRecipe>> MOON_DUST_SMELTING = SERIALIZERS.register(
        "moon_dust_smelting", MoonDustSmeltingRecipe.Serializer::new);

    private ModRecipes() {
    }

    public static void register() {
        SERIALIZERS.register();
    }
}
