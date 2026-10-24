package github.com.gengyoubo.adssc.compat.jade;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.flux.MoonFluxHolder;
import github.com.gengyoubo.adssc.flux.MoonFluxStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IDisplayHelper;
import snownee.jade.api.ui.IElementHelper;

/**
 * Shows the Moon Flux of any {@link MoonFluxHolder} as a bar laid out like Jade's own FE bar.
 * <p>
 * Jade's energy view always draws FE in red ({@code 0xFFAA0000} over {@code 0xFF660000}), so Moon Flux uses its own
 * provider with the same layout and number format, recoloured to the moondust purple.
 */
public enum MoonFluxProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, "moon_flux");
    private static final String STORED = "MoonFlux";
    private static final String CAPACITY = "MoonFluxCapacity";
    private static final int COLOR = 0xFF9B3FD6;
    private static final int SHADOW = 0xFF5B2290;

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MoonFluxHolder holder)) return;
        MoonFluxStorage storage = holder.getMoonFlux(null);
        if (storage == null) return;
        data.putInt(STORED, storage.getStored());
        data.putInt(CAPACITY, storage.getCapacity());
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        int capacity = data.getInt(CAPACITY);
        if (capacity <= 0) return;
        int stored = data.getInt(STORED);

        IDisplayHelper display = IDisplayHelper.get();
        Component text = Component.translatable("jade.ad_ssc.moon_flux",
                ChatFormatting.WHITE + display.humanReadableNumber(stored, "MF", false),
                display.humanReadableNumber(capacity, "MF", false))
            .withStyle(ChatFormatting.GRAY);
        IElementHelper helper = IElementHelper.get();
        tooltip.add(helper.progress((float) stored / capacity, text,
            helper.progressStyle().color(COLOR, SHADOW), BoxStyle.DEFAULT, true));
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
