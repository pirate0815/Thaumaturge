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
    public static final RegistryKey<ItemGroup> THAUMATURGE_TOOLS = RegistryKey.of(RegistryKeys.ITEM_GROUP, Thaumaturge.identifier("thaumaturge_tools"));

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
                        entries.add(new ItemStack(ModItems.AQUA_VIS_CRYSTAL));
                        entries.add(new ItemStack(ModItems.IGNIS_VIS_CRYSTAL));
                        entries.add(new ItemStack(ModItems.TERRA_VIS_CRYSTAL));
                        entries.add(new ItemStack(ModItems.ORDO_VIS_CRYSTAL));
                        entries.add(new ItemStack(ModItems.PERDITIO_VIS_CRYSTAL));
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
                    })
                    .build());
}