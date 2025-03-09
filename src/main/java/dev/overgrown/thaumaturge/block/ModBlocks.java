package dev.overgrown.thaumaturge.block;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.utils.BlockBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroups;

public class ModBlocks {
    public static final Block AMBER_BEARING_STONE = BlockBuilder.create("amber_bearing_stone")
            .setItemGroup(ItemGroups.BUILDING_BLOCKS)
            .withBlockSettings(settings -> settings.strength(4.0f))
            .withItemSettings(settings -> settings.maxCount(64))
            .buildAndRegister(Block::new);

    public static void register() {
        Thaumaturge.LOGGER.info("Registering Mod Blocks for " + Thaumaturge.MOD_ID);
    }
}