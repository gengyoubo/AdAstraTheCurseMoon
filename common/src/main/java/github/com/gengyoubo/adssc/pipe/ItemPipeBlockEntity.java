package github.com.gengyoubo.adssc.pipe;

import github.com.gengyoubo.adssc.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Logistical pipe. Pull faces take a batch of items from their inventory every {@link #PULL_INTERVAL} ticks;
 * items go round-robin to the inventories on push and normal faces across the network.
 * <p>
 * The one-slot buffer is a {@link WorldlyContainer}, so hoppers and other mods can insert through faces that accept
 * input on both loaders; nothing can take items back out of it.
 */
public class ItemPipeBlockEntity extends PipeBlockEntity implements WorldlyContainer {
    public static final int PULL_INTERVAL = 10;
    public static final int PULL_AMOUNT = 16;
    private static final int[] SLOTS = {0};
    private static final int[] NO_SLOTS = {};

    private ItemStack buffer = ItemStack.EMPTY;
    private int inputSides;

    public ItemPipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_PIPE.get(), pos, state);
    }

    public static boolean isAcceptor(Level level, BlockPos pos, Direction face) {
        return PipePlatform.findItemPort(level, pos, face) != null;
    }

    private record Target(ItemPipeBlockEntity pipe, Direction side, ItemPort port) {
    }

    @Override
    protected void tickNetwork(PipeNetwork network) {
        List<Target> targets = null;
        for (PipeBlockEntity member : network.members()) {
            ItemPipeBlockEntity pipe = (ItemPipeBlockEntity) member;
            if (pipe.buffer.isEmpty()) continue;
            if (targets == null) targets = targets(network);
            ItemStack rest = send(network, targets, pipe.buffer, pipe, pipe.inputSides, false);
            if (rest.getCount() != pipe.buffer.getCount()) {
                pipe.buffer = rest;
                if (rest.isEmpty()) pipe.inputSides = 0;
                pipe.setChanged();
            }
        }

        if (level == null || level.getGameTime() % PULL_INTERVAL != 0) return;
        for (PipeBlockEntity member : network.members()) {
            ItemPipeBlockEntity pipe = (ItemPipeBlockEntity) member;
            for (Direction side : Direction.values()) {
                if (pipe.getMode(side) != PipeMode.PULL || !pipe.facesAcceptor(side)) continue;
                ItemPort source = PipePlatform.findItemPort(level, pipe.worldPosition.relative(side), side.getOpposite());
                if (source == null) continue;
                if (targets == null) targets = targets(network);
                pipe.pull(network, targets, side, source);
            }
        }
    }

    private void pull(PipeNetwork network, List<Target> targets, Direction side, ItemPort source) {
        int from = bit(side);
        ItemStack kind = source.extract(
            stack -> send(network, targets, stack, this, from, true).getCount() < stack.getCount(), PULL_AMOUNT, true);
        if (kind.isEmpty()) return;
        int accepted = kind.getCount() - send(network, targets, kind, this, from, true).getCount();
        ItemStack taken = source.extract(stack -> ItemStack.isSameItemSameTags(stack, kind), accepted, false);
        ItemStack rest = send(network, targets, taken, this, from, false);
        if (!rest.isEmpty()) stash(source.insert(rest, false));
    }

    /** Keeps items that unexpectedly found no home, dropping them only if the buffer cannot take them either. */
    private void stash(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (buffer.isEmpty()) {
            buffer = stack;
        } else if (ItemStack.isSameItemSameTags(buffer, stack) && buffer.getCount() < buffer.getMaxStackSize()) {
            int moved = Math.min(stack.getCount(), buffer.getMaxStackSize() - buffer.getCount());
            buffer.grow(moved);
            stack.shrink(moved);
        }
        if (!stack.isEmpty() && level != null) Block.popResource(level, worldPosition, stack);
        setChanged();
    }

    private List<Target> targets(PipeNetwork network) {
        List<Target> targets = new ArrayList<>();
        for (PipeBlockEntity member : network.members()) {
            for (Direction side : Direction.values()) {
                if (!member.getMode(side).canOutput() || !member.facesAcceptor(side)) continue;
                ItemPort port = PipePlatform.findItemPort(level, member.getBlockPos().relative(side), side.getOpposite());
                if (port != null) targets.add(new Target((ItemPipeBlockEntity) member, side, port));
            }
        }
        return targets;
    }

    /**
     * Offers {@code stack} to the targets in turn, starting where the last delivery stopped and skipping the faces
     * in {@code skipSides} of {@code from}.
     *
     * @return what is left
     */
    private static ItemStack send(PipeNetwork network, List<Target> targets, ItemStack stack,
                                  ItemPipeBlockEntity from, int skipSides, boolean simulate) {
        ItemStack rest = stack;
        int count = targets.size();
        for (int k = 0; k < count && !rest.isEmpty(); k++) {
            int index = (network.cursor + k) % count;
            Target target = targets.get(index);
            if (target.pipe == from && (skipSides & bit(target.side)) != 0) continue;
            ItemStack after = target.port.insert(rest, simulate);
            if (!simulate && after.getCount() < rest.getCount()) network.cursor = index + 1;
            rest = after;
        }
        return rest == stack ? stack.copy() : rest;
    }

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return buffer.isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return buffer;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = buffer.split(amount);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = buffer;
        buffer = ItemStack.EMPTY;
        return removed;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        buffer = stack;
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void clearContent() {
        buffer = ItemStack.EMPTY;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return getMode(side).canInput() ? SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        if (side == null || !getMode(side).canInput()) return false;
        // Every insertion API asks this right before inserting, so it is the one place that knows the face.
        inputSides |= bit(side);
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        buffer = ItemStack.of(tag.getCompound("Buffer"));
        inputSides = tag.getByte("InputSides");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!buffer.isEmpty()) tag.put("Buffer", buffer.save(new CompoundTag()));
        tag.putByte("InputSides", (byte) inputSides);
    }
}
