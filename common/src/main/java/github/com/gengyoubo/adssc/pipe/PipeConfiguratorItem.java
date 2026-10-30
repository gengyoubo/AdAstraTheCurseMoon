package github.com.gengyoubo.adssc.pipe;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Cycles the connection mode of the pipe face that was clicked, like Mekanism's configurator.
 * Clicking an arm configures that arm; clicking the bare core configures the clicked face.
 * Sneaking cycles backwards.
 */
public class PipeConfiguratorItem extends Item {
    public PipeConfiguratorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof PipeBlock)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (!(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe)) return InteractionResult.PASS;

        Player player = context.getPlayer();
        Direction side = PipeBlock.targetSide(pos, context.getClickLocation(), context.getClickedFace());
        PipeMode mode = pipe.cycleMode(side, player == null || !player.isSecondaryUseActive());
        level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.3f, 1.2f);
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.ad_ssc.pipe_mode",
                Component.translatable("direction.ad_ssc." + side.getName()), mode.displayName()), true);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.ad_ssc.pipe_configurator").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.ad_ssc.pipe_configurator.modes").withStyle(ChatFormatting.DARK_PURPLE));
    }
}
