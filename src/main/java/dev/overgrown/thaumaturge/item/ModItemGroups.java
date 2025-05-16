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
    public static final RegistryKey<ItemGroup> THAUMATURGE_RESONANCE_MODIFIERS = RegistryKey.of(RegistryKeys.ITEM_GROUP, Thaumaturge.identifier("thaumaturge_resonance_modifiers"));

    // Custom Item Group for crystals
    public static final ItemGroup THAUMATURGE_CRYSTAL_GROUP = Registry.register(Registries.ITEM_GROUP,
            Thaumaturge.identifier("thaumaturge_crystals"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModBlocks.AER_CRYSTAL_CLUSTER))
                    .displayName(Text.translatable("item_group.thaumaturge.crystals"))
                    .entries((displayContext, entries) -> {
                        // Primal Aspects - First Level
                        entries.add(new ItemStack(ModItems.AER_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.AQUA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.IGNIS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.TERRA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.ORDO_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.PERDITIO_ASPECT_SHARD));

                        // Secondary Aspects - Second Level
                        entries.add(new ItemStack(ModItems.GELUM_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.LUX_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.METALLUM_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.MORTUUS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.MOTUS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.PERMUTATIO_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.POTENTIA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.VACUOS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.VICTUS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.VITREUS_ASPECT_SHARD));

                        // Tertiary Aspects - Third Level
                        entries.add(new ItemStack(ModItems.BESTIA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.EXANIMIS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.HERBA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.INSTRUMENTUM_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.PRAECANTATIO_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.SPIRITUS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.TENEBRAE_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.VINCULUM_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.VOLATUS_ASPECT_SHARD));

                        // Quaternary Aspects - Fourth Level
                        entries.add(new ItemStack(ModItems.ALIENIS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.ALKIMIA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.AURAM_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.AVERSIO_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.COGNITIO_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.DESIDERIUM_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.FABRICO_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.HUMANUS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.MACHINA_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.PRAEMUNIO_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.SENSUS_ASPECT_SHARD));
                        entries.add(new ItemStack(ModItems.VITIUM_ASPECT_SHARD));

                        // Crystal Blocks
                        // Aer Crystals
                        entries.add(new ItemStack(ModBlocks.BUDDING_AER_CRYSTAL));
                        entries.add(new ItemStack(ModBlocks.SMALL_AER_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.MEDIUM_AER_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.LARGE_AER_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.AER_CRYSTAL_CLUSTER));

                        // Ignis Crystals
                        entries.add(new ItemStack(ModBlocks.BUDDING_IGNIS_CRYSTAL));
                        entries.add(new ItemStack(ModBlocks.SMALL_IGNIS_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.MEDIUM_IGNIS_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.LARGE_IGNIS_CRYSTAL_BUD));
                        entries.add(new ItemStack(ModBlocks.IGNIS_CRYSTAL_CLUSTER));
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
                        entries.add(new ItemStack(ModItems.LESSER_FOCI));
                        entries.add(new ItemStack(ModItems.ADVANCED_FOCI));
                        entries.add(new ItemStack(ModItems.GREATER_FOCI));
                    })
                    .build());

    // Custom Item Group for Resonance Modifiers
    public static final ItemGroup THAUMATURGE_RESONANCE_MODIFIERS_GROUP = Registry.register(Registries.ITEM_GROUP,
            Thaumaturge.identifier("thaumaturge_resonance_modifiers"),
            FabricItemGroup.builder()
                    .icon(() -> new ItemStack(ModItems.SCATTER_RESONANCE_MODIFIER))
                    .displayName(Text.translatable("item_group.thaumaturge.resonance_modifiers"))
                    .entries((displayContext, entries) -> {
                        entries.add(new ItemStack(ModItems.SIMPLE_RESONANCE_MODIFIER));
                        entries.add(new ItemStack(ModItems.SCATTER_RESONANCE_MODIFIER));
                        entries.add(new ItemStack(ModItems.POWER_RESONANCE_MODIFIER));
                    })
                    .build());
}