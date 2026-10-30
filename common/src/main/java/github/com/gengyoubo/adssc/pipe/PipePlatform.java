package github.com.gengyoubo.adssc.pipe;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/**
 * Loader-specific glue for the pipes.
 * <p>
 * Fluids and energy go through Botarium, which Ad Astra already uses, so only items need a lookup of their own.
 * Exposing the pipes' buffers differs per loader: Forge attaches capabilities, Fabric registers Botarium getters
 * and relies on the Transfer API's built-in support for {@link net.minecraft.world.WorldlyContainer}.
 */
public final class PipePlatform {
    private PipePlatform() {
    }

    /** The items on the {@code side} face of the block at {@code pos}, or null if it holds none. */
    @Nullable
    @ExpectPlatform
    public static ItemPort findItemPort(Level level, BlockPos pos, Direction side) {
        throw new AssertionError("Platform implementation missing");
    }

    /** Makes the pipes' buffers visible to other blocks through the loader's storage APIs. */
    @ExpectPlatform
    public static void registerStorages() {
        throw new AssertionError("Platform implementation missing");
    }

    /** Called after a face opens or closes, so cached handles to the pipe are refreshed. */
    @ExpectPlatform
    public static void onSidesChanged(PipeBlockEntity pipe) {
        throw new AssertionError("Platform implementation missing");
    }
}
