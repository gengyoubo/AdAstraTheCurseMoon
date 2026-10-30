package github.com.gengyoubo.adssc.weather;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Keeps each surface's storm and flare clock across restarts. */
final class CurseSkyData extends SavedData {
    private static final String NAME = "ad_ssc_curse_sky";

    private final CurseSky sky = new CurseSky();

    static CurseSkyData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(CurseSkyData::load, CurseSkyData::new, NAME);
    }

    private static CurseSkyData load(CompoundTag tag) {
        var data = new CurseSkyData();
        data.sky.load(new CurseSky.Snapshot(
            tag.getInt("Roll"), tag.getInt("Storm"), tag.getInt("Flare"), tag.getInt("Cooldown")));
        return data;
    }

    CurseSky sky() {
        return sky;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        var state = sky.snapshot();
        tag.putInt("Roll", state.roll());
        tag.putInt("Storm", state.storm());
        tag.putInt("Flare", state.flare());
        tag.putInt("Cooldown", state.cooldown());
        return tag;
    }
}
