package dev.overgrown.thaumaturge.item;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.component.BookStateComponent;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.item.bonewits_dust.BonewitsDust;
import dev.overgrown.thaumaturge.utils.ItemBuilder;
import dev.overgrown.thaumaturge.item.apophenia.Apophenia;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
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

    //----------------------------------
    // Primal Aspects
    //----------------------------------
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
                    .fireproof()
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

    //----------------------------------
    // Secondary Aspects
    //----------------------------------
    public static final Item GELUM_ASPECT_SHARD = ItemBuilder.create("gelum_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item LUX_ASPECT_SHARD = ItemBuilder.create("lux_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item METALLUM_ASPECT_SHARD = ItemBuilder.create("metallum_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item MORTUUS_ASPECT_SHARD = ItemBuilder.create("mortuus_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item MOTUS_ASPECT_SHARD = ItemBuilder.create("motus_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item PERMUTATIO_ASPECT_SHARD = ItemBuilder.create("permutatio_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item POTENTIA_ASPECT_SHARD = ItemBuilder.create("potentia_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item VACUOS_ASPECT_SHARD = ItemBuilder.create("vacuos_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item VICTUS_ASPECT_SHARD = ItemBuilder.create("victus_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item VITREUS_ASPECT_SHARD = ItemBuilder.create("vitreus_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    //----------------------------------
    // Tertiary Aspects
    //----------------------------------
    public static final Item BESTIA_ASPECT_SHARD = ItemBuilder.create("bestia_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item EXANIMIS_ASPECT_SHARD = ItemBuilder.create("exanimis_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item HERBA_ASPECT_SHARD = ItemBuilder.create("herba_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item INSTRUMENTUM_ASPECT_SHARD = ItemBuilder.create("instrumentum_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item PRAECANTATIO_ASPECT_SHARD = ItemBuilder.create("praecantatio_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item SPIRITUS_ASPECT_SHARD = ItemBuilder.create("spiritus_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item TENEBRAE_ASPECT_SHARD = ItemBuilder.create("tenebrae_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item VINCULUM_ASPECT_SHARD = ItemBuilder.create("vinculum_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item VOLATUS_ASPECT_SHARD = ItemBuilder.create("volatus_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    //----------------------------------
    // Quaternary Aspects
    //----------------------------------
    public static final Item ALIENIS_ASPECT_SHARD = ItemBuilder.create("alienis_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ALKIMIA_ASPECT_SHARD = ItemBuilder.create("alkimia_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item AURAM_ASPECT_SHARD = ItemBuilder.create("auram_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item AVERSIO_ASPECT_SHARD = ItemBuilder.create("aversio_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item COGNITIO_ASPECT_SHARD = ItemBuilder.create("cognitio_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item DESIDERIUM_ASPECT_SHARD = ItemBuilder.create("desiderium_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item FABRICO_ASPECT_SHARD = ItemBuilder.create("fabrico_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item HUMANUS_ASPECT_SHARD = ItemBuilder.create("humanus_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item MACHINA_ASPECT_SHARD = ItemBuilder.create("machina_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item PRAEMUNIO_ASPECT_SHARD = ItemBuilder.create("praemunio_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item SENSUS_ASPECT_SHARD = ItemBuilder.create("sensus_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item VITIUM_ASPECT_SHARD = ItemBuilder.create("vitium_aspect_shard")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.RARE)
                    .fireproof()
            )
            .buildAndRegister();

    //======================================================================
    // Ingredients
    //======================================================================
    public static final Item BONEWITS_DUST = ItemBuilder.create("bonewits_dust")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister(BonewitsDust::new);

    public static final Item QUICKSILVER = ItemBuilder.create("quicksilver")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    public static final Item THAUMIC_LEATHER = ItemBuilder.create("thaumic_leather")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(64)
            )
            .buildAndRegister();

    //======================================================================
    // Special and Tool Items (crafted or used for unique functionalities)
    //======================================================================
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

    public static final Item AETHERIC_GOGGLES = ItemBuilder.create("aetheric_goggles")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .equippable(EquipmentSlot.HEAD)
            )
            .buildAndRegister();

    public static final Item SPINDLE = ItemBuilder.create("spindle")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
            )
            .buildAndRegister();

    //======================================================================
    // Foci (Used For Casting Spells When Using A Resonance Gauntlet)
    //======================================================================
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

    //======================================================================
    // Resonance Gauntlets
    //======================================================================
    public static final Item BASIC_CASTING_GAUNTLET = ItemBuilder.create("basic_casting_gauntlet")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
                    .component(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT)
                    .component(ModComponents.MAX_FOCI, 1)
                    .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(DyedColorComponent.DEFAULT_COLOR))
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

    //======================================================================
    // Resonance Modifiers
    //======================================================================
    public static final Item POWER_RESONANCE_MODIFIER = ItemBuilder.create("power_resonance_modifier")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
            )
            .buildAndRegister();

    public static final Item SIMPLE_RESONANCE_MODIFIER = ItemBuilder.create("simple_resonance_modifier")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
            )
            .buildAndRegister();

    public static final Item SCATTER_RESONANCE_MODIFIER = ItemBuilder.create("scatter_resonance_modifier")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
            )
            .buildAndRegister();
}