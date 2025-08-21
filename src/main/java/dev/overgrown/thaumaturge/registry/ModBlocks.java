package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.VesselBlock;
import dev.overgrown.thaumaturge.block.entity.VesselBlockEntity;
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

    public static void initialize() {
        Registry.register(Registries.BLOCK, Thaumaturge.identifier("vessel"), VESSEL);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, Thaumaturge.identifier("vessel"), VESSEL_BLOCK_ENTITY);
    }
}