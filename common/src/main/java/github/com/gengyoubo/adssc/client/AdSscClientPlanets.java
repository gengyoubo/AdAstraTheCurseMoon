package github.com.gengyoubo.adssc.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import earth.terrarium.adastra.api.client.events.AdAstraClientEvents;
import earth.terrarium.adastra.client.screens.PlanetsScreen;
import earth.terrarium.adastra.client.utils.DimensionRenderingUtils;
import github.com.gengyoubo.adssc.registry.ModDimensions;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/** The shared renderer for the Curse Moon catalog's animated star chart. */
public final class AdSscClientPlanets {
    private AdSscClientPlanets() {
    }

    public static void init() {
        AdAstraClientEvents.RenderSolarSystemEvent.register((graphics, system, width, height) -> {
            if (!ModDimensions.CURSE_MOON_SYSTEM.equals(system)) return;

            var tessellator = Tesselator.getInstance();
            var buffer = tessellator.getBuilder();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
            PlanetsScreen.drawCircles(0, 3, 0xff61418c, buffer, width, height);
            tessellator.end();

            // The Curse Moon is the center of this system; it does not orbit a sun.
            try {
                graphics.setColor(0.85f, 0.55f, 1.0f, 1.0f);
                graphics.blit(DimensionRenderingUtils.MOON, width / 2 - 12, height / 2 - 12,
                    0, 0, 24, 24, 24, 24);
            } finally {
                graphics.setColor(1, 1, 1, 1);
            }

            float time = (Util.getMillis() % 360_000L) / 1000f;
            satellite(graphics, DimensionRenderingUtils.PHOBOS, width, height, 30, time * 12);
            satellite(graphics, DimensionRenderingUtils.DEIMOS, width, height, 60, time * -8 + 120);
            satellite(graphics, DimensionRenderingUtils.VICINUS, width, height, 90, time * 5 + 240);
        });
    }

    private static void satellite(GuiGraphics graphics, ResourceLocation texture,
                                  int width, int height, int radius, float angle) {
        graphics.pose().pushPose();
        try {
            graphics.pose().translate(width / 2f, height / 2f, 0);
            graphics.pose().mulPose(Axis.ZP.rotationDegrees(angle));
            graphics.pose().translate(radius, 0, 0);
            graphics.blit(texture, -5, -5, 0, 0, 10, 10, 10, 10);
        } finally {
            graphics.pose().popPose();
        }
    }
}
