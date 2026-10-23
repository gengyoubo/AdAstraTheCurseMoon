package github.com.gengyoubo.adssc.mixin;

import github.com.gengyoubo.adssc.radiation.RadiationDataHolder;
import github.com.gengyoubo.adssc.radiation.RadiationExposure;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerRadiationMixin implements RadiationDataHolder {
    @Unique
    private final RadiationExposure adssc$exposure = new RadiationExposure();

    @Override
    public RadiationExposure adssc$radiation() {
        return adssc$exposure;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("RETURN"))
    private void adssc$saveRadiation(CompoundTag tag, CallbackInfo ci) {
        if (!adssc$exposure.active()) return;
        var state = adssc$exposure.snapshot();
        var saved = new CompoundTag();
        saved.putString("Original", state.original());
        saved.putString("Base", state.base());
        saved.putString("Applied", state.applied());
        saved.putInt("Stage", state.stage());
        saved.putInt("Level", state.level());
        saved.putInt("Ticks", state.ticks());
        tag.put("ad_ssc:curse_moon_radiation", saved);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("RETURN"))
    private void adssc$loadRadiation(CompoundTag tag, CallbackInfo ci) {
        var saved = tag.getCompound("ad_ssc:curse_moon_radiation");
        adssc$exposure.load(new RadiationExposure.Snapshot(
            saved.getString("Original"), saved.getString("Base"), saved.getString("Applied"),
            saved.getInt("Stage"), saved.getInt("Level"), saved.getInt("Ticks")));
    }
}
