package github.com.gengyoubo.adssc.client;

import earth.terrarium.adastra.client.ClientPlatformUtils;
import earth.terrarium.adastra.client.models.armor.SpaceSuitModel;
import github.com.gengyoubo.adssc.registry.ModItems;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Registers the worn-armor models of the morph-scale suits with Ad Astra's own suit renderer, so they look
 * identical to the originals. Must only ever be touched on the client.
 */
@Environment(EnvType.CLIENT)
public final class AdSscClientArmor {
    private AdSscClientArmor() {
    }

    public static void init() {
        ClientPlatformUtils.registerArmor(
            SpaceSuitModel.NETHERITE_SPACE_SUIT_TEXTURE,
            SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER,
            SpaceSuitModel::new,
            ModItems.MORPHSCALE_NETHERITE_SPACE_HELMET.get(),
            ModItems.MORPHSCALE_NETHERITE_SPACE_SUIT.get(),
            ModItems.MORPHSCALE_NETHERITE_SPACE_PANTS.get(),
            ModItems.MORPHSCALE_NETHERITE_SPACE_BOOTS.get());

        ClientPlatformUtils.registerArmor(
            SpaceSuitModel.JET_SUIT_TEXTURE,
            SpaceSuitModel.JET_SUIT_LAYER,
            SpaceSuitModel::new,
            ModItems.MORPHSCALE_JET_SUIT_HELMET.get(),
            ModItems.MORPHSCALE_JET_SUIT.get(),
            ModItems.MORPHSCALE_JET_SUIT_PANTS.get(),
            ModItems.MORPHSCALE_JET_SUIT_BOOTS.get());
    }
}
