package github.com.gengyoubo.adssc.pipe;

import net.minecraft.network.chat.Component;

/**
 * How one face of a pipe treats the block next to it, following Mekanism's transmitter connection types.
 * <p>
 * The configurator cycles them in declaration order. Between two pipes only {@link #NORMAL} and {@link #NONE}
 * matter: either face set to {@code NONE} splits the networks.
 */
public enum PipeMode {
    /** Accepts what the neighbour pushes in and hands network contents out to it. */
    NORMAL,
    /** Only hands network contents out; the neighbour cannot push in. */
    PUSH,
    /** Actively pulls from the neighbour and never hands anything back. */
    PULL,
    /** Disconnected. */
    NONE;

    private static final PipeMode[] VALUES = values();

    public boolean canOutput() {
        return this == NORMAL || this == PUSH;
    }

    public boolean canInput() {
        return this == NORMAL || this == PULL;
    }

    public PipeMode cycle(boolean forward, boolean towardsPipe) {
        if (towardsPipe) return this == NONE ? NORMAL : NONE;
        return VALUES[(ordinal() + (forward ? 1 : VALUES.length - 1)) % VALUES.length];
    }

    public Component displayName() {
        return Component.translatable("pipe.ad_ssc.mode." + name().toLowerCase(java.util.Locale.ROOT));
    }

    public static PipeMode byId(int id) {
        return id >= 0 && id < VALUES.length ? VALUES[id] : NORMAL;
    }
}
