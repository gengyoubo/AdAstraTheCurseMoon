package github.com.gengyoubo.adssc.mixin.client;

import earth.terrarium.adastra.client.models.armor.SpaceSuitModel;
import github.com.gengyoubo.adssc.registry.ModItems;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ad Astra's own {@code SpaceSuitModel.getLayerLocation} only recognises its three original chest pieces, which
 * makes the first-person suit arm disappear for the morph-scale chest pieces. This teaches it about ours.
 */
@Mixin(SpaceSuitModel.class)
public abstract class SpaceSuitModelMixin {
    @Inject(method = "getLayerLocation", at = @At("HEAD"), cancellable = true)
    private static void adssc$layerForMorphscale(ItemStack stack, CallbackInfoReturnable<ModelLayerLocation> cir) {
        if (stack.is(ModItems.MORPHSCALE_NETHERITE_SPACE_SUIT.get())) {
            cir.setReturnValue(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER);
        } else if (stack.is(ModItems.MORPHSCALE_JET_SUIT.get())) {
            cir.setReturnValue(SpaceSuitModel.JET_SUIT_LAYER);
        }
    }
}
