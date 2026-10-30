package github.com.gengyoubo.adssc.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import github.com.gengyoubo.adssc.client.CurseSkyClient;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Neither loader shares a fog event, so both use this hook for the moon dust storm. */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @Shadow
    private static float fogRed;
    @Shadow
    private static float fogGreen;
    @Shadow
    private static float fogBlue;

    @Inject(method = "setupColor", at = @At("TAIL"))
    private static void adssc$curseSkyColor(Camera camera, float partialTick, ClientLevel level,
                                            int renderDistance, float darkenWorld, CallbackInfo ci) {
        var color = CurseSkyClient.fogColor(camera, partialTick, fogRed, fogGreen, fogBlue);
        if (color == null) return;
        fogRed = color[0];
        fogGreen = color[1];
        fogBlue = color[2];
        RenderSystem.clearColor(fogRed, fogGreen, fogBlue, 0f);
    }

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void adssc$curseSkyFog(Camera camera, FogRenderer.FogMode mode, float renderDistance,
                                          boolean thickFog, float partialTick, CallbackInfo ci) {
        CurseSkyClient.setupFog(camera, mode, partialTick);
    }
}
