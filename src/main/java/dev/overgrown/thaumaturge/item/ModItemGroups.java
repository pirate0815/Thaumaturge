package dev.overgrown.thaumaturge.item;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroups {
    public static void register() {
        Thaumaturge.LOGGER.info("Registering Item Groups for " + Thaumaturge.MOD_ID);
    }

    public static final ItemGroup THAUMATURGE_CRYSTALS = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(Thaumaturge.MOD_ID, "thaumaturge_crystals"),
            FabricItemGroup.builder().icon(() -> new ItemStack(ModBlocks.AER_CRYSTAL_CLUSTER))
                    .displayName(Text.translatable("item_group.thaumaturge.crystals"))
                    .entries((displayContext, entries) -> {
                        entries.add(ModBlocks.AER_CRYSTAL_CLUSTER);
                        entries.add(ModItems.AER_VIS_CRYSTAL);
                        entries.add(ModItems.TERRA_VIS_CRYSTAL);
                        entries.add(ModItems.IGNIS_VIS_CRYSTAL);
                        entries.add(ModItems.AQUA_VIS_CRYSTAL);
                        entries.add(ModItems.ORDO_VIS_CRYSTAL);
                        entries.add(ModItems.PERDITIO_VIS_CRYSTAL);
                    })
                    .build());
}
