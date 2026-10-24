package github.com.gengyoubo.adssc.block;

import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import github.com.gengyoubo.adssc.flux.MoonFlux;
import github.com.gengyoubo.adssc.flux.MoonFluxContainer;
import github.com.gengyoubo.adssc.flux.MoonFluxHolder;
import github.com.gengyoubo.adssc.flux.MoonFluxStorage;
import github.com.gengyoubo.adssc.menu.MoonDustGeneratorMenu;
import github.com.gengyoubo.adssc.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Burns untreated moondust into Moon Flux and pushes it into neighbouring flux holders.
 * <p>
 * This is a plain {@link WorldlyContainer} rather than a {@code BaseContainerBlockEntity}: on Forge the latter
 * exposes an unsided item handler that lets hoppers pull fuel back out, while vanilla's container path honours
 * {@link #canTakeItemThroughFace} on both loaders.
 */
public class MoonDustGeneratorBlockEntity extends BlockEntity implements WorldlyContainer, MenuProvider, MoonFluxHolder {
    public static final TagKey<Item> FUELS =
        TagKey.create(Registries.ITEM, new ResourceLocation(AdAstraTheCurseMoon.MOD_ID, "moon_flux_fuels"));
    public static final int CAPACITY = 50_000;
    public static final int FLUX_PER_TICK = 40;
    /** One untreated moondust: 200 ticks x 40 MF/t = 8,000 MF. */
    public static final int BURN_TICKS = 200;
    public static final int OUTPUT_PER_SIDE = 200;
    private static final int[] SLOTS = {0};

    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final MoonFluxContainer flux = new MoonFluxContainer(CAPACITY, 0, OUTPUT_PER_SIDE, this::setChanged);
    private int burnTime;
    private int burnDuration;

    /** Menu sync sends each value as a short, so the flux amounts travel as two 16-bit halves. */
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> burnTime;
                case 1 -> burnDuration;
                case 2 -> flux.getStored() & 0xFFFF;
                case 3 -> flux.getStored() >>> 16;
                case 4 -> flux.getCapacity() & 0xFFFF;
                case 5 -> flux.getCapacity() >>> 16;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) burnTime = value;
            else if (index == 1) burnDuration = value;
        }

        @Override
        public int getCount() {
            return MoonDustGeneratorMenu.DATA_COUNT;
        }
    };

    public MoonDustGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOON_DUST_GENERATOR.get(), pos, state);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.is(FUELS);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MoonDustGeneratorBlockEntity generator) {
        // A full buffer pauses the burn instead of wasting fuel.
        boolean full = generator.flux.getStored() >= generator.flux.getCapacity();
        if (generator.burnTime > 0 && !full) {
            generator.burnTime--;
            generator.flux.generate(FLUX_PER_TICK);
        }
        if (generator.burnTime <= 0 && !full) {
            ItemStack fuel = generator.items.get(0);
            if (isFuel(fuel)) {
                fuel.shrink(1);
                generator.burnTime = generator.burnDuration = BURN_TICKS;
                generator.setChanged();
            }
        }

        MoonFlux.pushToNeighbours(level, pos, generator.flux, OUTPUT_PER_SIDE);

        boolean burning = generator.burnTime > 0;
        if (state.getValue(MoonDustGeneratorBlock.LIT) != burning) {
            level.setBlock(pos, state.setValue(MoonDustGeneratorBlock.LIT, burning), 3);
        }
    }

    @Override
    public MoonFluxStorage getMoonFlux(@Nullable Direction side) {
        return flux;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        ContainerHelper.loadAllItems(tag, items);
        flux.load(tag);
        burnTime = tag.getInt("BurnTime");
        burnDuration = tag.getInt("BurnDuration");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
        flux.save(tag);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnDuration", burnDuration);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.ad_ssc.moon_dust_generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MoonDustGeneratorMenu(id, inventory, this, data);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isFuel(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
