package github.com.gengyoubo.adssc.pipe;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/** One face of an inventory, wrapped from the loader's own item API by {@link PipePlatform#findItemPort}. */
public interface ItemPort {
    /** @return what did not fit; {@code stack} itself is left untouched */
    ItemStack insert(ItemStack stack, boolean simulate);

    /**
     * Removes up to {@code max} items of the first kind that {@code filter} accepts.
     * The filter receives a copy and may run simulated insertions elsewhere.
     *
     * @return what was (or, when simulating, would be) removed
     */
    ItemStack extract(Predicate<ItemStack> filter, int max, boolean simulate);
}
