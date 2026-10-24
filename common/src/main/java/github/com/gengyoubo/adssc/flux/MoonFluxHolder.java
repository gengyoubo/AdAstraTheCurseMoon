package github.com.gengyoubo.adssc.flux;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/** Implemented by block entities that hold Moon Flux. */
public interface MoonFluxHolder {
    /**
     * @param side the face being accessed, or null for an internal or sideless query
     * @return the storage exposed on that side, or null if the side is closed
     */
    @Nullable
    MoonFluxStorage getMoonFlux(@Nullable Direction side);
}
