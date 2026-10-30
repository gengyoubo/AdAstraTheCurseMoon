package github.com.gengyoubo.adssc.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A set of linked pipes of one kind that move contents as a unit.
 * <p>
 * Networks are rebuilt lazily: any change to a member's links invalidates the whole network, and the next member
 * to tick walks its links again. Only loaded pipes are walked, so a network that spans unloaded chunks is split until
 * they load; a pipe loading later absorbs the networks it finds.
 */
public final class PipeNetwork {
    private final List<PipeBlockEntity> members;
    private long lastTick = Long.MIN_VALUE;
    private boolean valid = true;
    /** Where the item pipes' round-robin resumes. */
    int cursor;

    private PipeNetwork(List<PipeBlockEntity> members) {
        this.members = members;
    }

    static PipeNetwork build(PipeBlockEntity origin) {
        List<PipeBlockEntity> members = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<PipeBlockEntity> queue = new ArrayDeque<>();
        queue.add(origin);
        seen.add(origin.getBlockPos());
        while (!queue.isEmpty()) {
            PipeBlockEntity pipe = queue.poll();
            members.add(pipe);
            for (Direction direction : Direction.values()) {
                PipeBlockEntity next = pipe.linkedPipe(direction);
                if (next != null && seen.add(next.getBlockPos())) queue.add(next);
            }
        }

        PipeNetwork network = new PipeNetwork(members);
        for (PipeBlockEntity member : members) {
            if (member.network != null) member.network.invalidate();
        }
        for (PipeBlockEntity member : members) {
            member.network = network;
        }
        return network;
    }

    public List<PipeBlockEntity> members() {
        return members;
    }

    void invalidate() {
        if (!valid) return;
        valid = false;
        for (PipeBlockEntity member : members) {
            if (member.network == this) member.network = null;
        }
    }

    /** True for the first member to ask during a game tick; the network moves contents once per tick. */
    boolean claimTick(long gameTime) {
        if (!valid || lastTick == gameTime) return false;
        lastTick = gameTime;
        return true;
    }
}
