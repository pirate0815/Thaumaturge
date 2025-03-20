package dev.overgrown.thaumaturge.block;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.clusters.AerCrystalClusterBlock;
import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.item.ModItemGroups;
import dev.overgrown.thaumaturge.utils.BlockBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroups;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class ModBlocks {
    public static final Block AMBER_BEARING_STONE = BlockBuilder.create("amber_bearing_stone")
            .setItemGroup(ItemGroups.BUILDING_BLOCKS)
            .withBlockSettings(settings -> settings.strength(4.0f))
            .withItemSettings(settings -> settings.maxCount(64))
            .buildAndRegister(Block::new);

    public static final Block AER_CRYSTAL_CLUSTER = BlockBuilder.create("aer_crystal_cluster")
            .setItemGroup(
                    ModItemGroups.THAUMATURGE_CRYSTALS
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(3f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister(settings -> new AerCrystalClusterBlock(UniformIntProvider.create(2, 5), settings));

    public static final Block VESSEL = BlockBuilder.create("vessel")
            .withBlockSettings(settings -> settings
                    .strength(3f)
                    .sounds(BlockSoundGroup.DEEPSLATE)
                    .requiresTool()
                    .nonOpaque()
            )
            .withItemSettings(settings -> settings
                    .maxCount(1)
            )
            .buildAndRegister(VesselBlock::new);


    public static void register() {
        Thaumaturge.LOGGER.info("Registering Blocks for " + Thaumaturge.MOD_ID);
    }
}