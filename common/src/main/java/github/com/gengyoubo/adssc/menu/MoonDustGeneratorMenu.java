package github.com.gengyoubo.adssc.menu;

import github.com.gengyoubo.adssc.block.MoonDustGeneratorBlockEntity;
import github.com.gengyoubo.adssc.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MoonDustGeneratorMenu extends AbstractContainerMenu {
    public static final int DATA_COUNT = 6;
    public static final int FUEL_X = 44;
    public static final int FUEL_Y = 53;
    private static final int PLAYER_SLOTS_START = 1;
    private static final int PLAYER_SLOTS_END = PLAYER_SLOTS_START + 36;

    private final Container container;
    private final ContainerData data;

    /** Client side: the server fills the slot and data in through menu sync. */
    public MoonDustGeneratorMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(1), new SimpleContainerData(DATA_COUNT));
    }

    public MoonDustGeneratorMenu(int id, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.MOON_DUST_GENERATOR.get(), id);
        checkContainerSize(container, 1);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        container.startOpen(inventory.player);

        addSlot(new Slot(container, 0, FUEL_X, FUEL_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MoonDustGeneratorBlockEntity.isFuel(stack);
            }
        });
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(data);
    }

    public boolean isBurning() {
        return data.get(0) > 0;
    }

    /** Remaining burn of the current fuel, scaled to {@code pixels}. */
    public int burnProgress(int pixels) {
        int duration = data.get(1);
        return duration == 0 ? 0 : data.get(0) * pixels / duration;
    }

    public int flux() {
        return join(2);
    }

    public int capacity() {
        return join(4);
    }

    public int fluxProgress(int pixels) {
        int capacity = capacity();
        return capacity == 0 ? 0 : (int) ((long) flux() * pixels / capacity);
    }

    private int join(int lowIndex) {
        return (data.get(lowIndex) & 0xFFFF) | (data.get(lowIndex + 1) & 0xFFFF) << 16;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, PLAYER_SLOTS_START, PLAYER_SLOTS_END, true)) return ItemStack.EMPTY;
        } else if (MoonDustGeneratorBlockEntity.isFuel(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (index < PLAYER_SLOTS_END - 9) {
            if (!moveItemStackTo(stack, PLAYER_SLOTS_END - 9, PLAYER_SLOTS_END, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, PLAYER_SLOTS_START, PLAYER_SLOTS_END - 9, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        container.stopOpen(player);
    }
}
