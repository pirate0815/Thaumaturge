package dev.overgrown.thaumaturge.item;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.utils.ItemBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Rarity;

public class ModItems {

    public static final Item LESSER_FOCI = ItemBuilder.create("lesser_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(1))
            .buildAndRegister();

    public static final Item BONEWITS_DUST = ItemBuilder.create("bonewits_dust")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings().maxCount(64).rarity(Rarity.RARE))
            .buildAndRegister();

    public static final Item AER_VIS_CRYSTAL = ItemBuilder.create("aer_vis_crystal")
            .setItemGroup(ModItemGroups.THAUMATURGE_CRYSTALS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item TERRA_VIS_CRYSTAL = ItemBuilder.create("terra_vis_crystal")
            .setItemGroup(ModItemGroups.THAUMATURGE_CRYSTALS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item IGNIS_VIS_CRYSTAL = ItemBuilder.create("ignis_vis_crystal")
            .setItemGroup(ModItemGroups.THAUMATURGE_CRYSTALS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item AQUA_VIS_CRYSTAL = ItemBuilder.create("aqua_vis_crystal")
            .setItemGroup(ModItemGroups.THAUMATURGE_CRYSTALS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item ORDO_VIS_CRYSTAL = ItemBuilder.create("ordo_vis_crystal")
            .setItemGroup(ModItemGroups.THAUMATURGE_CRYSTALS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item PERDITIO_VIS_CRYSTAL = ItemBuilder.create("perditio_vis_crystal")
            .setItemGroup(ModItemGroups.THAUMATURGE_CRYSTALS)
            .withSettings(new Item.Settings().maxCount(64))
            .buildAndRegister();

    public static final Item QUANTUM_STARFRAME = ItemBuilder.create("quantum_starframe")
            .setItemGroup(ModItemGroups.THAUMATURGE_TOOLS)
            .withSettings(new Item.Settings().maxCount(1))
            .buildAndRegister();

    public static void register() {
        Thaumaturge.LOGGER.info("Registering Items for " + Thaumaturge.MOD_ID);
    }
}
