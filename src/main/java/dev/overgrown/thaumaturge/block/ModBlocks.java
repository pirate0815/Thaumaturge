package dev.overgrown.thaumaturge.block;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.clusters.aer.*;
import dev.overgrown.thaumaturge.block.clusters.ignis.*;
import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.spell.impl.vacuos.block.BlackholeBlock;
import dev.overgrown.thaumaturge.utils.BlockBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroups;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class ModBlocks {
    public static void register() {
        Thaumaturge.LOGGER.info("Registering Blocks for " + Thaumaturge.MOD_ID);
    }

    public static final Block BLACKHOLE = BlockBuilder.create("blackhole")
            .setItemGroup(ItemGroups.FUNCTIONAL)
            .withBlockSettings(settings -> settings
                    .strength(4.0f)
                    .nonOpaque()
                    .dynamicBounds()
            )
            .withItemSettings(settings -> settings
                    .maxCount(1)
            )
            .buildAndRegister(BlackholeBlock::new);

    public static final Block BUDDING_AER_CRYSTAL = BlockBuilder.create("budding_aer_crystal")
            .setItemGroup(ItemGroups.NATURAL)
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(2f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
                    .ticksRandomly()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(BuddingAerCrystalBlock::new);

    public static final Block SMALL_AER_CRYSTAL_BUD = BlockBuilder.create("small_aer_crystal_bud")
            .setItemGroup(
                    ItemGroups.NATURAL
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(1.8f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(SmallAerCrystalBudBlock::new);

    public static final Block MEDIUM_AER_CRYSTAL_BUD = BlockBuilder.create("medium_aer_crystal_bud")
            .setItemGroup(
                    ItemGroups.NATURAL
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(1.8f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(MediumAerCrystalBudBlock::new);

    public static final Block LARGE_AER_CRYSTAL_BUD = BlockBuilder.create("large_aer_crystal_bud")
            .setItemGroup(
                    ItemGroups.NATURAL
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(1.8f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(LargeAerCrystalBudBlock::new);

    public static final Block AER_CRYSTAL_CLUSTER = BlockBuilder.create("aer_crystal_cluster")
            .setItemGroup(
                    ItemGroups.NATURAL
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(1.8f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(settings -> new AerCrystalClusterBlock(UniformIntProvider.create(2, 5), settings));

    public static final Block BUDDING_IGNIS_CRYSTAL = BlockBuilder.create("budding_ignis_crystal")
            .setItemGroup(ItemGroups.NATURAL)
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(2f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
                    .ticksRandomly()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(BuddingIgnisCrystalBlock::new);

    public static final Block SMALL_IGNIS_CRYSTAL_BUD = BlockBuilder.create("small_ignis_crystal_bud")
            .setItemGroup(
                    ItemGroups.NATURAL
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(1.8f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(SmallIgnisCrystalBudBlock::new);

    public static final Block MEDIUM_IGNIS_CRYSTAL_BUD = BlockBuilder.create("medium_ignis_crystal_bud")
            .setItemGroup(
                    ItemGroups.NATURAL
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(1.8f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(MediumIgnisCrystalBudBlock::new);

    public static final Block LARGE_IGNIS_CRYSTAL_BUD = BlockBuilder.create("large_ignis_crystal_bud")
            .setItemGroup(
                    ItemGroups.NATURAL
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(1.8f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(LargeIgnisCrystalBudBlock::new);

    public static final Block IGNIS_CRYSTAL_CLUSTER = BlockBuilder.create("ignis_crystal_cluster")
            .setItemGroup(
                    ItemGroups.NATURAL
            )
            .withBlockSettings(settings -> settings
                    .nonOpaque()
                    .strength(1.8f)
                    .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                    .requiresTool()
            )
            .withItemSettings(settings -> settings
                    .maxCount(64)
                    .rarity(Rarity.COMMON)
            )
            .buildAndRegister(settings -> new IgnisCrystalClusterBlock(UniformIntProvider.create(2, 5), settings));

    public static final Block VESSEL = BlockBuilder.create("vessel")
            .withBlockSettings(settings -> settings
                    .strength(3f)
                    .sounds(BlockSoundGroup.DEEPSLATE)
                    .requiresTool()
                    .nonOpaque()
            )
            .withItemSettings(settings -> settings
                    .maxCount(1)
                    .rarity(Rarity.UNCOMMON)
            )
            .buildAndRegister(VesselBlock::new);
}