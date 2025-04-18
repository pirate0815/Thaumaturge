package dev.overgrown.thaumaturge.item;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class ModItemGroups {

    public static void register() {
        Thaumaturge.LOGGER.info("Registering Item Groups for " + Thaumaturge.MOD_ID);
    }

    // Registry keys for the custom item groups
    public static final RegistryKey<ItemGroup> THAUMATURGE_CRYSTALS = RegistryKey.of(RegistryKeys.ITEM_GROUP, Thaumaturge.identifier("thaumaturge_crystals"));
    public static final RegistryKey<ItemGroup> THAUMATURGE_INGREDIENTS = RegistryKey.of(RegistryKeys.ITEM_GROUP, Thaumaturge.identifier("thaumaturge_ingredients"));
    public static final RegistryKey<ItemGroup> THAUMATURGE_TOOLS = RegistryKey.of(RegistryKeys.ITEM_GROUP, Thaumaturge.identifier("thaumaturge_tools"));
    public static final RegistryKey<ItemGroup> THAUMATURGE_FOCI = RegistryKey.of(RegistryKeys.ITEM_GROUP, Thaumaturge.identifier("thaumaturge_foci"));

    // Custom Item Group for crystals
    public static final ItemGroup THAUMATURGE_CRYSTAL_GROUP = Registry.register(Registries.ITEM_GROUP,
            Thaumaturge.identifier("thaumaturge_crystals"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.AER_CRYSTAL_CLUSTER))
                    .displayName(Text.translatable("item_group.thaumaturge.crystals"))
                    .entries((displayContext, entries) -> {
                        entries.add(new ItemStack(ModBlocks.BUDDING_AER_CRYSTAL));
                        entries.add(new ItemStack(ModBlocks.SMALL_AER_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.MEDIUM_AER_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.LARGE_AER_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.AER_CRYSTAL_CLUSTER));
                        entries.add(new ItemStack(ModItems.AER_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.AQUA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.IGNIS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.TERRA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.ORDO_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.PERDITIO_ASPECT_SHARD));
                    })
                    .build());

    // Custom Item Group for ingredients
    public static final ItemGroup THAUMATURGE_INGREDIENTS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Thaumaturge.identifier("thaumaturge_ingredients"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.BONEWITS_DUST))
                    .displayName(Text.translatable("item_group.thaumaturge.ingredients"))
                    .entries((displayContext, entries) -> {
                        entries.add(new ItemStack(ModItems.BONEWITS_DUST));
                        entries.add(new ItemStack(ModItems.QUICKSILVER));
                        entries.add(new ItemStack(ModItems.THAUMIC_LEATHER));
                    })
                    .build());

    // Custom Item Group for tools
    public static final ItemGroup THAUMATURGE_TOOLS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Thaumaturge.identifier("thaumaturge_tools"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.ASPECT_LENS))
                    .displayName(Text.translatable("item_group.thaumaturge.tools"))
                    .entries((displayContext, entries) -> {
                        entries.add(new ItemStack(ModItems.ASPECT_LENS));
                        entries.add(new ItemStack(ModItems.AETHERIC_GOGGLES));
                        entries.add(new ItemStack(ModItems.BASIC_CASTING_GAUNTLET));
                        entries.add(new ItemStack(ModItems.ADVANCED_MANIPULATION_GAUNTLET));
                        entries.add(new ItemStack(ModItems.ARCANE_ENGINEERING_GAUNTLET));
                        entries.add(new ItemStack(ModItems.SPINDLE));
                    })
                    .build());

    // Custom Item Group for Foci
    public static final ItemGroup THAUMATURGE_FOCI_GROUP = Registry.register(Registries.ITEM_GROUP,
            Thaumaturge.identifier("thaumaturge_foci"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.LESSER_FOCI))
                    .displayName(Text.translatable("item_group.thaumaturge.foci"))
                    .entries((displayContext, entries) -> {
                        // Empty Foci
                        entries.add(new ItemStack(ModItems.LESSER_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_FOCI));

                        // Aer (Air) Foci
                        entries.add(new ItemStack(ModItems.LESSER_AER_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_AER_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_AER_FOCI));

                        // Motus (Motion) Foci
                        entries.add(new ItemStack(ModItems.LESSER_MOTUS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_MOTUS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_MOTUS_FOCI));

                        // Aqua (Water) Foci
                        entries.add(new ItemStack(ModItems.LESSER_AQUA_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_AQUA_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_AQUA_FOCI));

                        // Ignis (Fire) Foci
                        entries.add(new ItemStack(ModItems.LESSER_IGNIS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_IGNIS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_IGNIS_FOCI));

                        // Ordo (Order) Foci
                        entries.add(new ItemStack(ModItems.LESSER_ORDO_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_ORDO_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_ORDO_FOCI));

                        // Perditio (Decay) Foci
                        entries.add(new ItemStack(ModItems.LESSER_PERDITIO_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_PERDITIO_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_PERDITIO_FOCI));

                        // Terra (Earth) Foci
                        entries.add(new ItemStack(ModItems.LESSER_TERRA_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_TERRA_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_TERRA_FOCI));

                        // Gelum (Cold) Foci
                        entries.add(new ItemStack(ModItems.LESSER_GELUM_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_GELUM_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_GELUM_FOCI));

                        // Lux (Light) Foci
                        entries.add(new ItemStack(ModItems.LESSER_LUX_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_LUX_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_LUX_FOCI));

                        // Metallum (Metal) Foci
                        entries.add(new ItemStack(ModItems.LESSER_METALLUM_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_METALLUM_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_METALLUM_FOCI));

                        // Mortuus (Death) Foci
                        entries.add(new ItemStack(ModItems.LESSER_MORTUUS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_MORTUUS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_MORTUUS_FOCI));

                        // Permutatio (Exchange) Foci
                        entries.add(new ItemStack(ModItems.LESSER_PERMUTATIO_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_PERMUTATIO_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_PERMUTATIO_FOCI));

                        // Potentia (Power) Foci
                        entries.add(new ItemStack(ModItems.LESSER_POTENTIA_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_POTENTIA_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_POTENTIA_FOCI));

                        // Vacuos (Void) Foci
                        entries.add(new ItemStack(ModItems.LESSER_VACUOS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_VACUOS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_VACUOS_FOCI));

                        // Victus (Life) Foci
                        entries.add(new ItemStack(ModItems.LESSER_VICTUS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_VICTUS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_VICTUS_FOCI));

                        // Vitreus (Crystal) Foci
                        entries.add(new ItemStack(ModItems.LESSER_VITREUS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_VITREUS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_VITREUS_FOCI));

                        // Bestia (Beast) Foci
                        entries.add(new ItemStack(ModItems.LESSER_BESTIA_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_BESTIA_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_BESTIA_FOCI));

                        // Exanimis (Undead) Foci
                        entries.add(new ItemStack(ModItems.LESSER_EXANIMIS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_EXANIMIS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_EXANIMIS_FOCI));

                        // Herba (Plant) Foci
                        entries.add(new ItemStack(ModItems.LESSER_HERBA_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_HERBA_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_HERBA_FOCI));

                        // Instrumentum (Tool) Foci
                        entries.add(new ItemStack(ModItems.LESSER_INSTRUMENTUM_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_INSTRUMENTUM_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_INSTRUMENTUM_FOCI));

                        // Praecantatio (Magic) Foci
                        entries.add(new ItemStack(ModItems.LESSER_PRAECANTATIO_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_PRAECANTATIO_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_PRAECANTATIO_FOCI));

                        // Spiritus (Spirit) Foci
                        entries.add(new ItemStack(ModItems.LESSER_SPIRITUS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_SPIRITUS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_SPIRITUS_FOCI));

                        // Tenebrae (Darkness) Foci
                        entries.add(new ItemStack(ModItems.LESSER_TENEBRAE_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_TENEBRAE_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_TENEBRAE_FOCI));

                        // Vinculum (Binding) Foci
                        entries.add(new ItemStack(ModItems.LESSER_VINCULUM_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_VINCULUM_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_VINCULUM_FOCI));

                        // Volatus (Flight) Foci
                        entries.add(new ItemStack(ModItems.LESSER_VOLATUS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_VOLATUS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_VOLATUS_FOCI));

                        // Alienis (Alien) Foci
                        entries.add(new ItemStack(ModItems.LESSER_ALIENIS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_ALIENIS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_ALIENIS_FOCI));

                        // Alkimia (Alchemy) Foci
                        entries.add(new ItemStack(ModItems.LESSER_ALKIMIA_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_ALKIMIA_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_ALKIMIA_FOCI));

                        // Auram (Aura) Foci
                        entries.add(new ItemStack(ModItems.LESSER_AURAM_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_AURAM_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_AURAM_FOCI));

                        // Aversion (Aversion) Foci
                        entries.add(new ItemStack(ModItems.LESSER_AVERSION_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_AVERSION_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_AVERSION_FOCI));

                        // Cognitio (Knowledge) Foci
                        entries.add(new ItemStack(ModItems.LESSER_COGNITIO_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_COGNITIO_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_COGNITIO_FOCI));

                        // Desiderium (Desire) Foci
                        entries.add(new ItemStack(ModItems.LESSER_DESIDERIUM_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_DESIDERIUM_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_DESIDERIUM_FOCI));

                        // Fabrico (Crafting) Foci
                        entries.add(new ItemStack(ModItems.LESSER_FABRICO_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_FABRICO_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_FABRICO_FOCI));

                        // Humanus (Human) Foci
                        entries.add(new ItemStack(ModItems.LESSER_HUMANUS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_HUMANUS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_HUMANUS_FOCI));

                        // Machina (Machine) Foci
                        entries.add(new ItemStack(ModItems.LESSER_MACHINA_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_MACHINA_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_MACHINA_FOCI));

                        // Praemunio (Defense) Foci
                        entries.add(new ItemStack(ModItems.LESSER_PRAEMUNIO_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_PRAEMUNIO_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_PRAEMUNIO_FOCI));

                        // Sensus (Senses) Foci
                        entries.add(new ItemStack(ModItems.LESSER_SENSUS_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_SENSUS_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_SENSUS_FOCI));

                        // Vitium (Taint) Foci
                        entries.add(new ItemStack(ModItems.LESSER_VITIUM_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_VITIUM_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_VITIUM_FOCI));
                    })
                    .build());
}