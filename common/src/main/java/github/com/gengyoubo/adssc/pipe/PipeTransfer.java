package github.com.gengyoubo.adssc.pipe;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Dependency-free distribution maths shared by the fluid and energy pipes. */
public final class PipeTransfer {
    private PipeTransfer() {
    }

    /** A destination that takes a plain quantity, such as millibuckets or energy units. */
    @FunctionalInterface
    public interface Sink {
        /** @return how much was (or, when simulating, would be) accepted */
        long insert(long amount, boolean simulate);
    }

    /**
     * Splits {@code amount} as evenly as the sinks allow, the way Mekanism networks share their buffer:
     * sinks that want less than an equal share are filled first and the rest is shared by the others.
     *
     * @return the total accepted
     */
    public static long split(List<? extends Sink> sinks, long amount, boolean simulate) {
        int count = sinks.size();
        if (amount <= 0 || count == 0) return 0;

        long[] wanted = new long[count];
        Integer[] order = new Integer[count];
        for (int i = 0; i < count; i++) {
            wanted[i] = Math.max(0, Math.min(amount, sinks.get(i).insert(amount, true)));
            order[i] = i;
        }
        Arrays.sort(order, Comparator.comparingLong(i -> wanted[i]));

        long[] shares = new long[count];
        long remaining = amount;
        for (int k = 0; k < count; k++) {
            int i = order[k];
            shares[i] = Math.min(wanted[i], remaining / (count - k));
            remaining -= shares[i];
        }
        // Integer division can leave a remainder that the fullest sinks still have room for.
        for (int k = count - 1; k >= 0 && remaining > 0; k--) {
            int i = order[k];
            long extra = Math.min(remaining, wanted[i] - shares[i]);
            shares[i] += extra;
            remaining -= extra;
        }

        if (simulate) return amount - remaining;
        long moved = 0;
        for (int i = 0; i < count; i++) {
            if (shares[i] > 0) moved += sinks.get(i).insert(shares[i], false);
        }
        return moved;
    }
}
