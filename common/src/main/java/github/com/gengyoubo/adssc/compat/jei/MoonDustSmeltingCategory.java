package github.com.gengyoubo.adssc.compat.jei;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.block.MoonDustSmeltingRecipe;
import github.com.gengyoubo.adssc.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class MoonDustSmeltingCategory implements IRecipeCategory<MoonDustSmeltingRecipe> {
    public static final RecipeType<MoonDustSmeltingRecipe> TYPE = new RecipeType<>(
        new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, "moon_dust_smelting"), MoonDustSmeltingRecipe.class);

    private final IDrawable icon;
    private final IDrawable arrow;
    private final IDrawable flame;
    private final IDrawable slot;

    public MoonDustSmeltingCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemStack(new ItemStack(ModItems.MOON_DUST_ORE.get()));
        arrow = gui.createAnimatedRecipeArrow(200);
        flame = gui.createAnimatedRecipeFlame(200);
        slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<MoonDustSmeltingRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.ad_ssc.moon_dust_smelting");
    }

    @Override
    public int getWidth() {
        return 176;
    }

    @Override
    public int getHeight() {
        return 76;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MoonDustSmeltingRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 20, 24)
            .addItemStack(AdSscJeiPlugin.oreStack(recipe.getOrigin())).setBackground(slot, -1, -1);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 132, 24)
            .addItemStack(recipe.getResultItem(RegistryAccess.EMPTY)).setBackground(slot, -1, -1);
    }

    @Override
    public void draw(MoonDustSmeltingRecipe recipe, IRecipeSlotsView slots, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        var origin = Component.translatable("tooltip.ad_ssc.moon_dust_ore.origin." + recipe.getOrigin());
        graphics.drawCenteredString(font, origin, 88, 3, 0xb667e6);
        arrow.draw(graphics, 76, 24);
        flame.draw(graphics, 21, 45);
        graphics.drawString(font, Component.translatable("jei.ad_ssc.smelting.time", recipe.getCookingTime() / 20f),
            52, 47, 0x404040, false);
        graphics.drawString(font, Component.translatable("jei.ad_ssc.smelting.experience", recipe.getExperience()),
            52, 61, 0x404040, false);
    }

    @Override
    public ResourceLocation getRegistryName(MoonDustSmeltingRecipe recipe) {
        return recipe.getId();
    }
}
