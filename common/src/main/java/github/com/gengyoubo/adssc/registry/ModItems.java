package github.com.gengyoubo.adssc.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import earth.terrarium.adastra.common.items.armor.JetSuitItem;
import earth.terrarium.adastra.common.items.armor.NetheriteSpaceSuitItem;
import earth.terrarium.adastra.common.items.armor.base.CustomDyeableArmorItem;
import earth.terrarium.adastra.common.items.armor.materials.JetSuitMaterial;
import earth.terrarium.adastra.common.items.armor.materials.NetheriteSpaceSuitMaterial;
import github.com.gengyoubo.adssc.AdAstraTheCurseMoon;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;

/**
 * Morph-scale counterparts of the Ad Astra netherite space suit and jet suit.
 * <p>
 * They reuse Ad Astra's armor classes and materials so oxygen, heat protection and jet flight keep working,
 * and they are listed in {@code shape-shifter-curse:morph_scale_item} so Shape Shifter Curse lets the player
 * wear them while transformed (refer to how the mod's own morph-scale armor is registered).
 */
public final class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(AdAstraTheCurseMoon.MOD_ID, Registries.ITEM);

    public static final RegistrySupplier<Item> MORPHSCALE_NETHERITE_SPACE_HELMET = ITEMS.register(
        "morphscale_netherite_space_helmet",
        () -> new CustomDyeableArmorItem(NetheriteSpaceSuitMaterial.MATERIAL, Type.HELMET, new Properties().fireResistant()));
    public static final RegistrySupplier<Item> MORPHSCALE_NETHERITE_SPACE_SUIT = ITEMS.register(
        "morphscale_netherite_space_suit",
        () -> new NetheriteSpaceSuitItem(NetheriteSpaceSuitMaterial.MATERIAL, Type.CHESTPLATE, 2000L, new Properties().fireResistant()));
    public static final RegistrySupplier<Item> MORPHSCALE_NETHERITE_SPACE_PANTS = ITEMS.register(
        "morphscale_netherite_space_pants",
        () -> new CustomDyeableArmorItem(NetheriteSpaceSuitMaterial.MATERIAL, Type.LEGGINGS, new Properties().fireResistant()));
    public static final RegistrySupplier<Item> MORPHSCALE_NETHERITE_SPACE_BOOTS = ITEMS.register(
        "morphscale_netherite_space_boots",
        () -> new CustomDyeableArmorItem(NetheriteSpaceSuitMaterial.MATERIAL, Type.BOOTS, new Properties().fireResistant()));

    public static final RegistrySupplier<Item> MORPHSCALE_JET_SUIT_HELMET = ITEMS.register(
        "morphscale_jet_suit_helmet",
        () -> new CustomDyeableArmorItem(JetSuitMaterial.MATERIAL, Type.HELMET, new Properties().fireResistant()));
    public static final RegistrySupplier<Item> MORPHSCALE_JET_SUIT = ITEMS.register(
        "morphscale_jet_suit",
        () -> new JetSuitItem(JetSuitMaterial.MATERIAL, Type.CHESTPLATE, 4000, 1000000, new Properties().fireResistant()));
    public static final RegistrySupplier<Item> MORPHSCALE_JET_SUIT_PANTS = ITEMS.register(
        "morphscale_jet_suit_pants",
        () -> new CustomDyeableArmorItem(JetSuitMaterial.MATERIAL, Type.LEGGINGS, new Properties().fireResistant()));
    public static final RegistrySupplier<Item> MORPHSCALE_JET_SUIT_BOOTS = ITEMS.register(
        "morphscale_jet_suit_boots",
        () -> new CustomDyeableArmorItem(JetSuitMaterial.MATERIAL, Type.BOOTS, new Properties().fireResistant()));

    private ModItems() {
    }

    public static void register() {
        ITEMS.register();
    }
}
