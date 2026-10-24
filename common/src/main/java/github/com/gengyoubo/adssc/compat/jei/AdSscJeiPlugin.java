package github.com.gengyoubo.adssc.compat.jei;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.block.MoonDustOreBlock;
import github.com.gengyoubo.adssc.block.MoonDustOreItem;
import github.com.gengyoubo.adssc.block.MoonDustSmeltingRecipe;
import github.com.gengyoubo.adssc.registry.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.FurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

/** Discovered by JEI only; common/server initialization never loads this optional integration. */
@JeiPlugin
public final class AdSscJeiPlugin implements IModPlugin {
    private List<MoonDustSmeltingRecipe> smeltingRecipes = List.of();

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, "jei");
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // Only origin affects the recipe: names and unrelated NBT must not split the ingredient.
        registration.registerSubtypeInterpreter(ModItems.MOON_DUST_ORE.get(),
            (stack, context) -> "origin=" + MoonDustOreItem.origin(stack));
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        registration.addExtraItemStacks(IntStream.rangeClosed(1, MoonDustOreBlock.MAX_ORIGIN)
            .mapToObj(AdSscJeiPlugin::oreStack).toList());
    }

    public static ItemStack oreStack(int origin) {
        var stack = new ItemStack(ModItems.MOON_DUST_ORE.get());
        if (origin > 0) {
            stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG)
                .putString(MoonDustOreBlock.ORIGIN.getName(), Integer.toString(origin));
        }
        return stack;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new MoonDustSmeltingCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        var level = Minecraft.getInstance().level;
        smeltingRecipes = level == null ? List.of() : level.getRecipeManager()
            .getAllRecipesFor(RecipeType.SMELTING).stream()
            .filter(MoonDustSmeltingRecipe.class::isInstance)
            .map(MoonDustSmeltingRecipe.class::cast)
            .sorted(Comparator.comparingInt(MoonDustSmeltingRecipe::getOrigin))
            .toList();
        registration.addRecipes(MoonDustSmeltingCategory.TYPE, smeltingRecipes);
        registration.addItemStackInfo(IntStream.rangeClosed(0, MoonDustOreBlock.MAX_ORIGIN)
            .mapToObj(AdSscJeiPlugin::oreStack).toList(), Component.translatable("jei.ad_ssc.moon_dust_ore.info"));
        registration.addItemStackInfo(List.of(
            new ItemStack(ModItems.MORPHSCALE_NETHERITE_SPACE_HELMET.get()),
            new ItemStack(ModItems.MORPHSCALE_NETHERITE_SPACE_SUIT.get()),
            new ItemStack(ModItems.MORPHSCALE_NETHERITE_SPACE_PANTS.get()),
            new ItemStack(ModItems.MORPHSCALE_NETHERITE_SPACE_BOOTS.get()),
            new ItemStack(ModItems.MORPHSCALE_JET_SUIT_HELMET.get()),
            new ItemStack(ModItems.MORPHSCALE_JET_SUIT.get()),
            new ItemStack(ModItems.MORPHSCALE_JET_SUIT_PANTS.get()),
            new ItemStack(ModItems.MORPHSCALE_JET_SUIT_BOOTS.get())
        ), Component.translatable("jei.ad_ssc.morphscale_suit.info"));
        AdAstraTheCurseMoon.LOGGER.info("JEI: registered {} origin-aware moon dust smelting recipes", smeltingRecipes.size());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(Items.FURNACE), MoonDustSmeltingCategory.TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(FurnaceMenu.class, MenuType.FURNACE,
            MoonDustSmeltingCategory.TYPE, 0, 1, 3, 36);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        // JEI's vanilla smelting category would discard the origin condition and show plain ore
        // producing every yield. Keep only our precise, tagged-input representation in the UI.
        runtime.getRecipeManager().hideRecipes(RecipeTypes.SMELTING, new ArrayList<SmeltingRecipe>(smeltingRecipes));
    }

    @Override
    public void onRuntimeUnavailable() {
        smeltingRecipes = List.of();
    }
}
