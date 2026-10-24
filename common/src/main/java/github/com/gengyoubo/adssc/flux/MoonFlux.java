package github.com.gengyoubo.adssc.flux;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Locale;

/** Lookup and transfer helpers for Moon Flux. */
public final class MoonFlux {
    private MoonFlux() {
    }

    /** The storage on the {@code side} face of the block at {@code pos}, or null. */
    @Nullable
    public static MoonFluxStorage find(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getBlockEntity(pos) instanceof MoonFluxHolder holder ? holder.getMoonFlux(side) : null;
    }

    /**
     * Pushes flux from {@code source} into every neighbour that accepts it, at most {@code maxPerSide} each.
     *
     * @return the total amount moved
     */
    public static int pushToNeighbours(Level level, BlockPos pos, MoonFluxStorage source, int maxPerSide) {
        int moved = 0;
        for (Direction direction : Direction.values()) {
            if (source.getStored() <= 0) break;
            MoonFluxStorage target = find(level, pos.relative(direction), direction.getOpposite());
            if (target == null || !target.canInsert()) continue;
            int offered = source.extract(maxPerSide, true);
            int accepted = target.insert(offered, false);
            source.extract(accepted, false);
            moved += accepted;
        }
        return moved;
    }

    /** "1,234 / 50,000 MF" */
    public static Component describe(int stored, int capacity) {
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.ROOT);
        return Component.translatable("tooltip.ad_ssc.moon_flux.amount", format.format(stored), format.format(capacity));
    }
}
