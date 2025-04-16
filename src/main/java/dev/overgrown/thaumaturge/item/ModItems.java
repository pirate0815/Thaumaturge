package dev.overgrown.thaumaturge.item;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.component.BookStateComponent;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.item.bonewits_dust.BonewitsDust;
import dev.overgrown.thaumaturge.utils.ItemBuilder;
import dev.overgrown.thaumaturge.item.apophenia.Apophenia;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.util.Rarity;

/**
 * ModItems is responsible for instantiating and registering all custom mod items for the Thaumaturge mod.
 * Each item is built through the ItemBuilder helper, which abstracts away registry and grouping operations.
 */
public class ModItems {

    /**
     * Register mod items during initialization.
     * This method serves as a hook so that when the mod starts, item registration is logged and processed.
     */
    public static void register() {
        // Logging to provide feedback during mod initialization, especially useful for debugging.
        Thaumaturge.LOGGER.info("Registering Items for " + Thaumaturge.MOD_ID);
    }

    //======================================================================
    // Aspect Shard Items (Fundamental for "starting" the game, required in everything)
    //======================================================================

    public static final Item AER_ASPECT_SHARD = ItemBuilder.create("aer_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item TERRA_ASPECT_SHARD = ItemBuilder.create("terra_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item IGNIS_ASPECT_SHARD = ItemBuilder.create("ignis_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item AQUA_ASPECT_SHARD = ItemBuilder.create("aqua_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item ORDO_ASPECT_SHARD = ItemBuilder.create("ordo_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item PERDITIO_ASPECT_SHARD = ItemBuilder.create("perditio_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    //======================================================================
    // Special and Tool Items (crafted or used for unique functionalities)
    //======================================================================

    public static final Item BONEWITS_DUST = ItemBuilder.create("bonewits_dust")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister(BonewitsDust::new);

    public static final Item APOPHENIA = ItemBuilder.create("apophenia")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .component(ModComponents.BOOK_STATE, BookStateComponent.DEFAULT)
            )
            .buildAndRegister(Apophenia::new);

    public static final Item ASPECT_LENS = ItemBuilder.create("aspect_lens")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
            )
            .buildAndRegister();

    public static final Item LESSER_FOCI = ItemBuilder.create("lesser_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_FOCI = ItemBuilder.create("advanced_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_FOCI = ItemBuilder.create("greater_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    public static final Item AER_LESSER_FOCI = ItemBuilder.create("aer_lesser_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item AER_ADVANCED_FOCI = ItemBuilder.create("aer_advanced_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item AER_GREATER_FOCI = ItemBuilder.create("aer_greater_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    public static final Item AETHERIC_GOGGLES = ItemBuilder.create("aetheric_goggles")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .equippable(EquipmentSlot.HEAD)
            )
            .buildAndRegister();

    public static final Item BASIC_CASTING_GAUNTLET = ItemBuilder.create("basic_casting_gauntlet")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
                    .component(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT)
                    .component(ModComponents.MAX_FOCI, 1)
            )
            .buildAndRegister();

    public static final Item ADVANCED_MANIPULATION_GAUNTLET = ItemBuilder.create("advanced_manipulation_gauntlet")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
                    .component(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT)
                    .component(ModComponents.MAX_FOCI, 2)
            )
            .buildAndRegister();

    public static final Item ARCANE_ENGINEERING_GAUNTLET = ItemBuilder.create("arcane_engineering_gauntlet")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
                    .component(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT)
                    .component(ModComponents.MAX_FOCI, 3)
            )
            .buildAndRegister();
}