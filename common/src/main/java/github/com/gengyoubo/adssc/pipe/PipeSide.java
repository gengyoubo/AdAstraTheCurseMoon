package github.com.gengyoubo.adssc.pipe;

import net.minecraft.util.StringRepresentable;

/** What the model draws on one face: nothing, a plain arm, or an arm ending in a push or pull ring. */
public enum PipeSide implements StringRepresentable {
    NONE("none"),
    CONNECTED("connected"),
    PUSH("push"),
    PULL("pull");

    private final String name;

    PipeSide(String name) {
        this.name = name;
    }

    /** The look of an arm leading into a machine or inventory configured with {@code mode}. */
    public static PipeSide forAcceptor(PipeMode mode) {
        return switch (mode) {
            case NORMAL -> CONNECTED;
            case PUSH -> PUSH;
            case PULL -> PULL;
            case NONE -> NONE;
        };
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
