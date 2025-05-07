package dev.overgrown.thaumaturge.utils;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Blocks {
        /**
         * Helper method to create a block tag with the mod's namespace
         */
        private static TagKey<Block> createTag(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(Thaumaturge.MOD_ID, name));
        }

        public static final TagKey<Block> AER_CRYSTAL_BUDS_AND_CLUSTER = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("aer_crystal_buds_and_cluster"));
        public static final TagKey<Block> AQUA_CRYSTAL_BUDS_AND_CLUSTER = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("aqua_crystal_buds_and_cluster"));
        public static final TagKey<Block> IGNIS_CRYSTAL_BUDS_AND_CLUSTER = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("ignis_crystal_buds_and_cluster"));
        public static final TagKey<Block> ORDO_CRYSTAL_BUDS_AND_CLUSTER = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("ordo_crystal_buds_and_cluster"));
        public static final TagKey<Block> PERDITIO_CRYSTAL_BUDS_AND_CLUSTER = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("perditio_crystal_buds_and_cluster"));
        public static final TagKey<Block> TERRA_CRYSTAL_BUDS_AND_CLUSTER = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("terra_crystal_buds_and_cluster"));
    }

    public static class Items {
        /**
         * Helper method to create an item tag with the mod's namespace
         */
        private static TagKey<Item> createTag(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(Thaumaturge.MOD_ID, name));
        }

        // Tag for all foci items - used to identify items that can be inserted into gauntlets
        public static final TagKey<Item> FOCI = TagKey.of(RegistryKeys.ITEM, Thaumaturge.identifier("foci"));
        public static final TagKey<Item> RESONANCE_MODIFIERS = TagKey.of(RegistryKeys.ITEM, Thaumaturge.identifier("resonance_modifiers"));
    }
}