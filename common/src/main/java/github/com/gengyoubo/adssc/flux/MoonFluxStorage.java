package github.com.gengyoubo.adssc.flux;

/**
 * A buffer of Moon Flux (MF), the energy produced from moon dust.
 * <p>
 * Moon Flux is its own energy and does not convert to or from Forge Energy or Ad Astra's energy on its own;
 * only blocks that expose a storage through {@link MoonFluxHolder} can exchange it.
 */
public interface MoonFluxStorage {
    int getStored();

    int getCapacity();

    /** @return how much was (or, when simulating, would be) accepted */
    int insert(int amount, boolean simulate);

    /** @return how much was (or, when simulating, would be) removed */
    int extract(int amount, boolean simulate);

    default boolean canInsert() {
        return true;
    }

    default boolean canExtract() {
        return true;
    }
}
