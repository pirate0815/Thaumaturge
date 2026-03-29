package dev.overgrown.aspectslib.registry;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.entity.aura_node.AuraNodeEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntities {
    public static final EntityType<AuraNodeEntity> AURA_NODE = FabricEntityTypeBuilder.create(SpawnGroup.AMBIENT, AuraNodeEntity::new)
            .dimensions(EntityDimensions.fixed(0.5f, 0.5f))
            .trackRangeBlocks(64).trackedUpdateRate(3)
            .build();

    public static void register() {
        Registry.register(Registries.ENTITY_TYPE, AspectsLib.identifier("aura_node"), AURA_NODE);
    }
}