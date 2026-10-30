package github.com.gengyoubo.adssc.pipe;

import java.util.ArrayList;
import java.util.List;

/** Dependency-free tests of the pipes' even split, run by :common:testPipes and the build's check task. */
public final class PipeTransferTest {
    private static int assertions;

    public static void main(String[] args) {
        var a = new Tank(1000);
        var b = new Tank(1000);
        var c = new Tank(1000);
        equal(900L, PipeTransfer.split(List.of(a, b, c), 900, false), "everything fits");
        equal(List.of(300L, 300L, 300L), stored(a, b, c), "equal shares");

        a = new Tank(1000);
        b = new Tank(1000);
        c = new Tank(1000);
        equal(10L, PipeTransfer.split(List.of(a, b, c), 10, false), "rounding remainder is still delivered");
        equal(10L, a.stored + b.stored + c.stored, "no amount lost to integer division");

        a = new Tank(50);
        b = new Tank(1000);
        c = new Tank(1000);
        equal(900L, PipeTransfer.split(List.of(a, b, c), 900, false), "small sink does not block the rest");
        equal(List.of(50L, 425L, 425L), stored(a, b, c), "the small sink's unused share goes to the others");

        a = new Tank(100);
        b = new Tank(100);
        equal(200L, PipeTransfer.split(List.of(a, b), 500, true), "simulation reports what fits");
        equal(List.of(0L, 0L), stored(a, b), "simulation changes nothing");
        equal(200L, PipeTransfer.split(List.of(a, b), 500, false), "overflow stops at capacity");

        equal(0L, PipeTransfer.split(List.of(), 500, false), "no sinks");
        equal(0L, PipeTransfer.split(List.of(new Tank(100)), 0, false), "nothing to send");

        System.out.println("PipeTransferTest: " + assertions + " assertions passed");
    }

    private static List<Long> stored(Tank... tanks) {
        List<Long> amounts = new ArrayList<>();
        for (Tank tank : tanks) amounts.add(tank.stored);
        return amounts;
    }

    private static void equal(Object expected, Object actual, String message) {
        assertions++;
        if (!expected.equals(actual)) {
            throw new AssertionError(message + ": expected " + expected + " but was " + actual);
        }
    }

    private static final class Tank implements PipeTransfer.Sink {
        private final long capacity;
        private long stored;

        private Tank(long capacity) {
            this.capacity = capacity;
        }

        @Override
        public long insert(long amount, boolean simulate) {
            long accepted = Math.min(amount, capacity - stored);
            if (!simulate) stored += accepted;
            return accepted;
        }
    }
}
