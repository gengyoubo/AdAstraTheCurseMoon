package github.com.gengyoubo.adssc.flux;

import net.minecraft.nbt.CompoundTag;

/** A plain Moon Flux buffer with separate transfer limits for insertion and extraction. */
public class MoonFluxContainer implements MoonFluxStorage {
    private final int capacity;
    private final int maxInsert;
    private final int maxExtract;
    private final Runnable onChanged;
    private int stored;

    public MoonFluxContainer(int capacity, int maxInsert, int maxExtract, Runnable onChanged) {
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
        this.onChanged = onChanged;
    }

    @Override
    public int getStored() {
        return stored;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public int insert(int amount, boolean simulate) {
        return add(Math.min(amount, maxInsert), simulate);
    }

    @Override
    public int extract(int amount, boolean simulate) {
        int removed = Math.max(0, Math.min(Math.min(amount, maxExtract), stored));
        if (!simulate && removed > 0) {
            stored -= removed;
            onChanged.run();
        }
        return removed;
    }

    @Override
    public boolean canInsert() {
        return maxInsert > 0;
    }

    @Override
    public boolean canExtract() {
        return maxExtract > 0;
    }

    /** Adds flux without the insertion limit, for the owner's own production. */
    public int generate(int amount) {
        return add(amount, false);
    }

    private int add(int amount, boolean simulate) {
        int added = Math.max(0, Math.min(amount, capacity - stored));
        if (!simulate && added > 0) {
            stored += added;
            onChanged.run();
        }
        return added;
    }

    public void save(CompoundTag tag) {
        tag.putInt("MoonFlux", stored);
    }

    public void load(CompoundTag tag) {
        stored = Math.max(0, Math.min(tag.getInt("MoonFlux"), capacity));
    }
}
