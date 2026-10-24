package github.com.gengyoubo.adssc.block;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Shows which body a silk-touched ore came from; placement itself is handled by the vanilla BlockStateTag. */
public class MoonDustOreItem extends BlockItem {
    public MoonDustOreItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int origin = origin(stack);
        if (origin > 0) {
            tooltip.add(Component.translatable("tooltip.ad_ssc.moon_dust_ore.origin." + origin).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    /** The origin a silk-touched stack carries, or 0 for a plain one. */
    public static int origin(ItemStack stack) {
        var tag = stack.getTagElement(BLOCK_STATE_TAG);
        if (tag == null) return 0;
        try {
            int origin = Integer.parseInt(tag.getString(MoonDustOreBlock.ORIGIN.getName()));
            return origin >= 0 && origin <= MoonDustOreBlock.MAX_ORIGIN ? origin : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
