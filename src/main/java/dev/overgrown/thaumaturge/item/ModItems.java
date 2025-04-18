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

    public static final Item SPINDLE = ItemBuilder.create("spindle")
            .setItemGroup(ItemGroups.TOOLS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
            )
            .buildAndRegister();

    //======================================================================
    // Empty Foci (Used For Casting Spells When Using A Resonance Gauntlet)
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
    // Aer (Air) Foci
    //======================================================================
    public static final Item LESSER_AER_FOCI = ItemBuilder.create("lesser_aer_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_AER_FOCI = ItemBuilder.create("advanced_aer_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_AER_FOCI = ItemBuilder.create("greater_aer_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Motus (Motion) Foci
    //======================================================================
    public static final Item LESSER_MOTUS_FOCI = ItemBuilder.create("lesser_motus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_MOTUS_FOCI = ItemBuilder.create("advanced_motus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_MOTUS_FOCI = ItemBuilder.create("greater_motus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Aqua (Water) Foci
    //======================================================================
    public static final Item LESSER_AQUA_FOCI = ItemBuilder.create("lesser_aqua_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_AQUA_FOCI = ItemBuilder.create("advanced_aqua_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_AQUA_FOCI = ItemBuilder.create("greater_aqua_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Ignis (Fire) Foci
    //======================================================================
    public static final Item LESSER_IGNIS_FOCI = ItemBuilder.create("lesser_ignis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_IGNIS_FOCI = ItemBuilder.create("advanced_ignis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_IGNIS_FOCI = ItemBuilder.create("greater_ignis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Ordo (Order) Foci
    //======================================================================
    public static final Item LESSER_ORDO_FOCI = ItemBuilder.create("lesser_ordo_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_ORDO_FOCI = ItemBuilder.create("advanced_ordo_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_ORDO_FOCI = ItemBuilder.create("greater_ordo_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Perditio (Decay) Foci
    //======================================================================
    public static final Item LESSER_PERDITIO_FOCI = ItemBuilder.create("lesser_perditio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_PERDITIO_FOCI = ItemBuilder.create("advanced_perditio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_PERDITIO_FOCI = ItemBuilder.create("greater_perditio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Terra (Earth) Foci
    //======================================================================
    public static final Item LESSER_TERRA_FOCI = ItemBuilder.create("lesser_terra_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_TERRA_FOCI = ItemBuilder.create("advanced_terra_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_TERRA_FOCI = ItemBuilder.create("greater_terra_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Gelum (Cold) Foci
    //======================================================================
    public static final Item LESSER_GELUM_FOCI = ItemBuilder.create("lesser_gelum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_GELUM_FOCI = ItemBuilder.create("advanced_gelum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_GELUM_FOCI = ItemBuilder.create("greater_gelum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Lux (Light) Foci
    //======================================================================
    public static final Item LESSER_LUX_FOCI = ItemBuilder.create("lesser_lux_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_LUX_FOCI = ItemBuilder.create("advanced_lux_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_LUX_FOCI = ItemBuilder.create("greater_lux_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Metallum (Metal) Foci
    //======================================================================
    public static final Item LESSER_METALLUM_FOCI = ItemBuilder.create("lesser_metallum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_METALLUM_FOCI = ItemBuilder.create("advanced_metallum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_METALLUM_FOCI = ItemBuilder.create("greater_metallum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Mortuus (Death) Foci
    //======================================================================
    public static final Item LESSER_MORTUUS_FOCI = ItemBuilder.create("lesser_mortuus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_MORTUUS_FOCI = ItemBuilder.create("advanced_mortuus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_MORTUUS_FOCI = ItemBuilder.create("greater_mortuus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Permutatio (Exchange) Foci
    //======================================================================
    public static final Item LESSER_PERMUTATIO_FOCI = ItemBuilder.create("lesser_permutatio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_PERMUTATIO_FOCI = ItemBuilder.create("advanced_permutatio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_PERMUTATIO_FOCI = ItemBuilder.create("greater_permutatio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Potentia (Power) Foci
    //======================================================================
    public static final Item LESSER_POTENTIA_FOCI = ItemBuilder.create("lesser_potentia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_POTENTIA_FOCI = ItemBuilder.create("advanced_potentia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_POTENTIA_FOCI = ItemBuilder.create("greater_potentia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Vacuos (Void) Foci
    //======================================================================
    public static final Item LESSER_VACUOS_FOCI = ItemBuilder.create("lesser_vacuos_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_VACUOS_FOCI = ItemBuilder.create("advanced_vacuos_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_VACUOS_FOCI = ItemBuilder.create("greater_vacuos_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Victus (Life) Foci
    //======================================================================
    public static final Item LESSER_VICTUS_FOCI = ItemBuilder.create("lesser_victus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_VICTUS_FOCI = ItemBuilder.create("advanced_victus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_VICTUS_FOCI = ItemBuilder.create("greater_victus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Vitreus (Crystal) Foci
    //======================================================================
    public static final Item LESSER_VITREUS_FOCI = ItemBuilder.create("lesser_vitreus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_VITREUS_FOCI = ItemBuilder.create("advanced_vitreus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_VITREUS_FOCI = ItemBuilder.create("greater_vitreus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Bestia (Beast) Foci
    //======================================================================
    public static final Item LESSER_BESTIA_FOCI = ItemBuilder.create("lesser_bestia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_BESTIA_FOCI = ItemBuilder.create("advanced_bestia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_BESTIA_FOCI = ItemBuilder.create("greater_bestia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Exanimis (Undead) Foci
    //======================================================================
    public static final Item LESSER_EXANIMIS_FOCI = ItemBuilder.create("lesser_exanimis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_EXANIMIS_FOCI = ItemBuilder.create("advanced_exanimis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_EXANIMIS_FOCI = ItemBuilder.create("greater_exanimis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Herba (Plant) Foci
    //======================================================================
    public static final Item LESSER_HERBA_FOCI = ItemBuilder.create("lesser_herba_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_HERBA_FOCI = ItemBuilder.create("advanced_herba_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_HERBA_FOCI = ItemBuilder.create("greater_herba_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Instrumentum (Tool) Foci
    //======================================================================
    public static final Item LESSER_INSTRUMENTUM_FOCI = ItemBuilder.create("lesser_instrumentum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_INSTRUMENTUM_FOCI = ItemBuilder.create("advanced_instrumentum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_INSTRUMENTUM_FOCI = ItemBuilder.create("greater_instrumentum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Praecantatio (Magic) Foci
    //======================================================================
    public static final Item LESSER_PRAECANTATIO_FOCI = ItemBuilder.create("lesser_praecantatio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_PRAECANTATIO_FOCI = ItemBuilder.create("advanced_praecantatio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_PRAECANTATIO_FOCI = ItemBuilder.create("greater_praecantatio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Spiritus (Spirit) Foci
    //======================================================================
    public static final Item LESSER_SPIRITUS_FOCI = ItemBuilder.create("lesser_spiritus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_SPIRITUS_FOCI = ItemBuilder.create("advanced_spiritus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_SPIRITUS_FOCI = ItemBuilder.create("greater_spiritus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Tenebrae (Darkness) Foci
    //======================================================================
    public static final Item LESSER_TENEBRAE_FOCI = ItemBuilder.create("lesser_tenebrae_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_TENEBRAE_FOCI = ItemBuilder.create("advanced_tenebrae_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_TENEBRAE_FOCI = ItemBuilder.create("greater_tenebrae_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Vinculum (Binding) Foci
    //======================================================================
    public static final Item LESSER_VINCULUM_FOCI = ItemBuilder.create("lesser_vinculum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_VINCULUM_FOCI = ItemBuilder.create("advanced_vinculum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_VINCULUM_FOCI = ItemBuilder.create("greater_vinculum_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Volatus (Flight) Foci
    //======================================================================
    public static final Item LESSER_VOLATUS_FOCI = ItemBuilder.create("lesser_volatus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_VOLATUS_FOCI = ItemBuilder.create("advanced_volatus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_VOLATUS_FOCI = ItemBuilder.create("greater_volatus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Alienis (Alien) Foci
    //======================================================================
    public static final Item LESSER_ALIENIS_FOCI = ItemBuilder.create("lesser_alienis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_ALIENIS_FOCI = ItemBuilder.create("advanced_alienis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_ALIENIS_FOCI = ItemBuilder.create("greater_alienis_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Alkimia (Alchemy) Foci
    //======================================================================
    public static final Item LESSER_ALKIMIA_FOCI = ItemBuilder.create("lesser_alkimia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_ALKIMIA_FOCI = ItemBuilder.create("advanced_alkimia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_ALKIMIA_FOCI = ItemBuilder.create("greater_alkimia_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Auram (Aura) Foci
    //======================================================================
    public static final Item LESSER_AURAM_FOCI = ItemBuilder.create("lesser_auram_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_AURAM_FOCI = ItemBuilder.create("advanced_auram_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_AURAM_FOCI = ItemBuilder.create("greater_auram_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Aversion (Aversion) Foci
    //======================================================================
    public static final Item LESSER_AVERSION_FOCI = ItemBuilder.create("lesser_aversion_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_AVERSION_FOCI = ItemBuilder.create("advanced_aversion_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_AVERSION_FOCI = ItemBuilder.create("greater_aversion_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Cognitio (Knowledge) Foci
    //======================================================================
    public static final Item LESSER_COGNITIO_FOCI = ItemBuilder.create("lesser_cognitio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_COGNITIO_FOCI = ItemBuilder.create("advanced_cognitio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_COGNITIO_FOCI = ItemBuilder.create("greater_cognitio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Desiderium (Desire) Foci
    //======================================================================
    public static final Item LESSER_DESIDERIUM_FOCI = ItemBuilder.create("lesser_desiderium_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_DESIDERIUM_FOCI = ItemBuilder.create("advanced_desiderium_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_DESIDERIUM_FOCI = ItemBuilder.create("greater_desiderium_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Fabrico (Crafting) Foci
    //======================================================================
    public static final Item LESSER_FABRICO_FOCI = ItemBuilder.create("lesser_fabrico_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_FABRICO_FOCI = ItemBuilder.create("advanced_fabrico_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_FABRICO_FOCI = ItemBuilder.create("greater_fabrico_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Humanus (Human) Foci
    //======================================================================
    public static final Item LESSER_HUMANUS_FOCI = ItemBuilder.create("lesser_humanus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_HUMANUS_FOCI = ItemBuilder.create("advanced_humanus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_HUMANUS_FOCI = ItemBuilder.create("greater_humanus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Machina (Machine) Foci
    //======================================================================
    public static final Item LESSER_MACHINA_FOCI = ItemBuilder.create("lesser_machina_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_MACHINA_FOCI = ItemBuilder.create("advanced_machina_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_MACHINA_FOCI = ItemBuilder.create("greater_machina_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Praemunio (Defense) Foci
    //======================================================================
    public static final Item LESSER_PRAEMUNIO_FOCI = ItemBuilder.create("lesser_praemunio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_PRAEMUNIO_FOCI = ItemBuilder.create("advanced_praemunio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_PRAEMUNIO_FOCI = ItemBuilder.create("greater_praemunio_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Sensus (Senses) Foci
    //======================================================================
    public static final Item LESSER_SENSUS_FOCI = ItemBuilder.create("lesser_sensus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_SENSUS_FOCI = ItemBuilder.create("advanced_sensus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_SENSUS_FOCI = ItemBuilder.create("greater_sensus_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();

    //======================================================================
    // Vitium (Taint) Foci
    //======================================================================
    public static final Item LESSER_VITIUM_FOCI = ItemBuilder.create("lesser_vitium_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister();

    public static final Item ADVANCED_VITIUM_FOCI = ItemBuilder.create("advanced_vitium_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.RARE)
            )
            .buildAndRegister();

    public static final Item GREATER_VITIUM_FOCI = ItemBuilder.create("greater_vitium_foci")
            .setItemGroup(ItemGroups.INGREDIENTS)
            .withSettings(new Item.Settings()
                    .maxCount(1)
                    .rarity(Rarity.EPIC)
            )
            .buildAndRegister();
}