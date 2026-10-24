package github.com.gengyoubo.adssc.compat.jade;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Discovered by Jade only (the annotation on Forge, the {@code jade} entrypoint on Fabric); nothing else loads it.
 * <p>
 * Registered against every block so future Moon Flux machines show up without touching this class.
 */
@WailaPlugin
public final class AdSscJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MoonFluxProvider.INSTANCE, BlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MoonFluxProvider.INSTANCE, Block.class);
    }
}
