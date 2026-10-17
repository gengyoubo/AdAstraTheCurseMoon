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
import net.minecraft.client.renderer.GameRenderer;

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
            PlanetsScreen.drawCircles(1, 1, 0xff61418c, buffer, width, height);
            tessellator.end();

            graphics.blit(DimensionRenderingUtils.RED_SUN, width / 2 - 8, height / 2 - 8,
                0, 0, 16, 16, 16, 16);

            graphics.pose().pushPose();
            try {
                graphics.pose().translate(width / 2f, height / 2f, 0);
                graphics.pose().mulPose(Axis.ZP.rotationDegrees((Util.getMillis() / 160f) % 360f));
                graphics.pose().translate(60, 0, 0);
                graphics.setColor(0.85f, 0.55f, 1.0f, 1.0f);
                graphics.blit(DimensionRenderingUtils.MOON, -8, -8, 0, 0, 16, 16, 16, 16);
            } finally {
                graphics.setColor(1, 1, 1, 1);
                graphics.pose().popPose();
            }
        });
    }
}
