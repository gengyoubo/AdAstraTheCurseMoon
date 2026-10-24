package github.com.gengyoubo.adssc.block;

import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Exercises the actual vanilla (Fabric) cooking parser and recipe wire format. */
public final class MoonDustSmeltingRecipeTest {
    private static int assertions;

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var serializer = new MoonDustSmeltingRecipe.Serializer();
        for (int origin = 0; origin <= 4; origin++) {
            var id = new ResourceLocation("ad_ssc", "test_origin_" + origin);
            var json = recipeJson(origin, origin + 1);
            var recipe = serializer.fromJson(id, json);
            equal(origin, recipe.getOrigin(), "origin decoded");
            equal(origin + 1, recipe.getResultItem(RegistryAccess.EMPTY).getCount(), "object result count decoded");
            equal(true, json.get("result").isJsonObject(), "parsing does not mutate source JSON");
            for (int inputOrigin = 0; inputOrigin <= 4; inputOrigin++) {
                var input = new ItemStack(Items.STONE);
                if (inputOrigin > 0) input.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG)
                    .putString("origin", Integer.toString(inputOrigin));
                equal(origin == inputOrigin, recipe.matches(new SimpleContainer(input), null), "only matching ore origin accepted");
            }
            var buffer = new FriendlyByteBuf(Unpooled.buffer());
            try {
                serializer.toNetwork(buffer, recipe);
                var decoded = serializer.fromNetwork(id, buffer);
                equal(origin, decoded.getOrigin(), "origin survives server/client sync");
                equal(origin + 1, decoded.getResultItem(RegistryAccess.EMPTY).getCount(), "yield survives server/client sync");
                equal(recipe.getCookingTime(), decoded.getCookingTime(), "time survives sync");
                equal(0, buffer.readableBytes(), "entire packet consumed");
            } finally {
                buffer.release();
            }
        }
        var json = recipeJson(0, 1);
        json.addProperty("result", "minecraft:redstone");
        equal(1, serializer.fromJson(new ResourceLocation("ad_ssc", "test_string"), json)
            .getResultItem(RegistryAccess.EMPTY).getCount(), "standard string result supported");
        rejects(() -> serializer.fromJson(new ResourceLocation("ad_ssc", "test_bad_origin"), recipeJson(5, 1)));
        rejects(() -> serializer.fromJson(new ResourceLocation("ad_ssc", "test_zero"), recipeJson(0, 0)));
        rejects(() -> serializer.fromJson(new ResourceLocation("ad_ssc", "test_overflow"), recipeJson(0, 65)));
        var malformed = new ItemStack(Items.STONE);
        malformed.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString("origin", "invalid");
        equal(0, MoonDustOreItem.origin(malformed), "invalid origin NBT falls back to plain ore");
        System.out.println("Moon dust smelting: " + assertions + " assertions passed.");
    }

    private static JsonObject recipeJson(int origin, int count) {
        var json = new JsonObject();
        json.addProperty("origin", origin);
        var ingredient = new JsonObject();
        ingredient.addProperty("item", "minecraft:stone");
        json.add("ingredient", ingredient);
        var result = new JsonObject();
        result.addProperty("item", "minecraft:redstone");
        result.addProperty("count", count);
        json.add("result", result);
        json.addProperty("experience", 0.2f);
        json.addProperty("cookingtime", 200);
        return json;
    }

    private static void equal(Object expected, Object actual, String message) {
        assertions++;
        if (!expected.equals(actual)) throw new AssertionError(message + ": expected " + expected + ", got " + actual);
    }

    private static void rejects(Runnable operation) {
        assertions++;
        try {
            operation.run();
        } catch (IllegalArgumentException expected) {
            return;
        }
        throw new AssertionError("Invalid recipe was accepted");
    }
}
