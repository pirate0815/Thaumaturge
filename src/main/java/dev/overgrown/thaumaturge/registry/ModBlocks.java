package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.aspect_clusters.*;
import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.block.vessel.entity.VesselBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlocks {
    public static final Block VESSEL = new VesselBlock(FabricBlockSettings.copyOf(Blocks.CAULDRON));
    public static final BlockEntityType<VesselBlockEntity> VESSEL_BLOCK_ENTITY =
            FabricBlockEntityTypeBuilder.create(VesselBlockEntity::new, VESSEL).build();

    // Aspect Clusters
    public static final Block AER_CLUSTER = new AerClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block AQUA_CLUSTER = new AquaClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block IGNIS_CLUSTER = new IgnisClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block TERRA_CLUSTER = new TerraClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block ORDO_CLUSTER = new OrdoClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));
    public static final Block PERDITIO_CLUSTER = new PerditioClusterBlock(FabricBlockSettings.copyOf(Blocks.AMETHYST_CLUSTER));

    public static void initialize() {
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("vessel"), VESSEL);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Thaumaturge.identifier("vessel"), VESSEL_BLOCK_ENTITY);

        // Register Aspect Clusters
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("aer_cluster"), AER_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("aqua_cluster"), AQUA_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("ignis_cluster"), IGNIS_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("terra_cluster"), TERRA_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("ordo_cluster"), ORDO_CLUSTER);
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("perditio_cluster"), PERDITIO_CLUSTER);
    }
}