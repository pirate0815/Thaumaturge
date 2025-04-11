package dev.overgrown.thaumaturge.item;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.utils.ItemBuilder;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Rarity;

public class ModItems {

    public static void register() {
        Thaumaturge.LOGGER.info("Registering Items for " + Thaumaturge.MOD_ID);
    }

    public static final Item LESSER_FOCI = ItemBuilder.create("lesser_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(1).rarity(Rarity.UNCOMMON))
            .buildAndRegister();

    public static final Item ADVANCED_FOCI = ItemBuilder.create("advanced_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(1).rarity(Rarity.RARE))
            .buildAndRegister();

    public static final Item GREATER_FOCI = ItemBuilder.create("greater_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(1).rarity(Rarity.EPIC))
            .buildAndRegister();

    public static final Item BONEWITS_DUST = ItemBuilder.create("bonewits_dust")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(64).rarity(Rarity.RARE))
            .buildAndRegister();

    public static final Item AER_VIS_CRYSTAL = ItemBuilder.create("aer_vis_crystal")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item TERRA_VIS_CRYSTAL = ItemBuilder.create("terra_vis_crystal")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item IGNIS_VIS_CRYSTAL = ItemBuilder.create("ignis_vis_crystal")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item AQUA_VIS_CRYSTAL = ItemBuilder.create("aqua_vis_crystal")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item ORDO_VIS_CRYSTAL = ItemBuilder.create("ordo_vis_crystal")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item PERDITIO_VIS_CRYSTAL = ItemBuilder.create("perditio_vis_crystal")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item ASPECT_LENS = ItemBuilder.create("aspect_lens")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings().maxCount(1))
            .buildAndRegister();

    public static final Item AETHERIC_GOGGLES = ItemBuilder.create("aetheric_goggles")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings().maxCount(1).equippable(EquipmentSlot.HEAD))
            .buildAndRegister();

    public static final Item RESONANCE_MONOCLE = ItemBuilder.create("resonance_monocle")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings().maxCount(1).equippable(EquipmentSlot.HEAD))
            .buildAndRegister();
}
