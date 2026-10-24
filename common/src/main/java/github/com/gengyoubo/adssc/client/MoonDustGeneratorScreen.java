package github.com.gengyoubo.adssc.client;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.block.MoonDustGeneratorBlockEntity;
import github.com.gengyoubo.adssc.flux.MoonFlux;
import github.com.gengyoubo.adssc.menu.MoonDustGeneratorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.text.NumberFormat;
import java.util.Locale;

public class MoonDustGeneratorScreen extends AbstractContainerScreen<MoonDustGeneratorMenu> {
    private static final ResourceLocation TEXTURE =
        new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, "textures/gui/container/moon_dust_generator.png");
    // Positions inside the background; the sprites sit to the right of it in the same texture.
    private static final int MOON_X = 45, MOON_Y = 36, MOON_SIZE = 14, MOON_U = 176, MOON_V = 0;
    private static final int BAR_X = 98, BAR_Y = 17, BAR_W = 16, BAR_H = 52, BAR_U = 176, BAR_V = 14;
    private static final int TEXT_X = 121;
    private static final int TEXT_COLOR = 0x404040, ACTIVE_COLOR = 0x8d3fb0;

    public MoonDustGeneratorScreen(MoonDustGeneratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (isHovering(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX, mouseY)) {
            graphics.renderTooltip(font, MoonFlux.describe(menu.flux(), menu.capacity()), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (menu.isBurning()) {
            // The crescent drains from the top like a furnace flame.
            int lit = menu.burnProgress(MOON_SIZE - 1) + 1;
            int skip = MOON_SIZE - lit;
            graphics.blit(TEXTURE, leftPos + MOON_X, topPos + MOON_Y + skip, MOON_U, MOON_V + skip, MOON_SIZE, lit);
        }
        int filled = menu.fluxProgress(BAR_H);
        if (filled > 0) {
            int skip = BAR_H - filled;
            graphics.blit(TEXTURE, leftPos + BAR_X, topPos + BAR_Y + skip, BAR_U, BAR_V + skip, BAR_W, filled);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.ROOT);
        graphics.drawString(font, format.format(menu.flux()), TEXT_X, 22, TEXT_COLOR, false);
        graphics.drawString(font, "/ " + format.format(menu.capacity()), TEXT_X, 32, TEXT_COLOR, false);
        graphics.drawString(font, "MF", TEXT_X, 42, TEXT_COLOR, false);
        // A full buffer pauses the burn, so it reads as idle even while the fuel is lit.
        boolean generating = menu.isBurning() && menu.flux() < menu.capacity();
        Component rate = generating
            ? Component.translatable("gui.ad_ssc.moon_flux.generating", MoonDustGeneratorBlockEntity.FLUX_PER_TICK)
            : Component.translatable("gui.ad_ssc.moon_flux.idle");
        graphics.drawString(font, rate, TEXT_X, 60, generating ? ACTIVE_COLOR : TEXT_COLOR, false);
    }
}
