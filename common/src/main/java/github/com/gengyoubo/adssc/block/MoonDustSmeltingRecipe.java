package github.com.gengyoubo.adssc.block;

import com.google.gson.JsonObject;
import github.com.gengyoubo.adssc.registry.ModRecipes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;

/**
 * A furnace recipe that only accepts moon dust ore of one {@link MoonDustOreBlock#ORIGIN}.
 * <p>
 * The furnace asks a recipe for its result without the input (see {@code AbstractFurnaceBlockEntity.burn}), so the
 * yield cannot be computed from the stack. Instead each origin has its own recipe with a fixed result, and
 * {@link #matches} picks the one for the ore being smelted.
 */
public class MoonDustSmeltingRecipe extends SmeltingRecipe {
    private final int origin;

    public MoonDustSmeltingRecipe(SmeltingRecipe base, int origin) {
        super(base.getId(), base.getGroup(), base.category(), base.getIngredients().get(0),
            base.getResultItem(null), base.getExperience(), base.getCookingTime());
        this.origin = origin;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return super.matches(container, level) && MoonDustOreItem.origin(container.getItem(0)) == origin;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.MOON_DUST_SMELTING.get();
    }

    public int getOrigin() {
        return origin;
    }

    /** Reads the usual smelting fields plus an {@code origin}. */
    public static class Serializer implements RecipeSerializer<MoonDustSmeltingRecipe> {
        @Override
        public MoonDustSmeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
            int origin = GsonHelper.getAsInt(json, "origin");
            if (origin < 0 || origin > MoonDustOreBlock.MAX_ORIGIN) {
                throw new IllegalArgumentException("Moon dust origin out of range: " + origin);
            }
            // Fabric's vanilla serializer accepts a string result only, while Forge also
            // accepts an ItemStack object. Normalize first so counts work identically on both.
            JsonObject normalized = json.deepCopy();
            int count = 1;
            if (GsonHelper.isObjectNode(json, "result")) {
                JsonObject result = GsonHelper.getAsJsonObject(json, "result");
                normalized.addProperty("result", GsonHelper.getAsString(result, "item"));
                count = GsonHelper.getAsInt(result, "count", 1);
            }
            SmeltingRecipe base = RecipeSerializer.SMELTING_RECIPE.fromJson(id, normalized);
            if (count < 1 || count > base.getResultItem(null).getMaxStackSize()) {
                throw new IllegalArgumentException("Moon dust result count out of range: " + count);
            }
            base.getResultItem(null).setCount(count);
            return new MoonDustSmeltingRecipe(base, origin);
        }

        @Override
        public MoonDustSmeltingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            SmeltingRecipe base = RecipeSerializer.SMELTING_RECIPE.fromNetwork(id, buf);
            return new MoonDustSmeltingRecipe(base, buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, MoonDustSmeltingRecipe recipe) {
            RecipeSerializer.SMELTING_RECIPE.toNetwork(buf, recipe);
            buf.writeVarInt(recipe.origin);
        }
    }
}
