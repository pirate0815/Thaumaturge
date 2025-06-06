package dev.overgrown.thaumaturge.block;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.vessel.VesselBlockEntity;
import dev.overgrown.thaumaturge.spell.impl.vacuos.entity.BlackholeBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {
    public static final BlockEntityType<VesselBlockEntity> VESSEL = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Thaumaturge.identifier("vessel"),
            FabricBlockEntityTypeBuilder.create(VesselBlockEntity::new, ModBlocks.VESSEL).build()
    );

    public static final BlockEntityType<BlackholeBlockEntity> BLACKHOLE = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            Thaumaturge.identifier("blackhole"),
            FabricBlockEntityTypeBuilder.create(BlackholeBlockEntity::new, ModBlocks.BLACKHOLE).build()
    );

    public static void register() {
        Thaumaturge.LOGGER.info("Registering Block Entities for " + Thaumaturge.MOD_ID);
    }
}