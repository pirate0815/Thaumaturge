package dev.overgrown.thaumaturge.entity;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.impl.metallum.entity.MetalShardEntity;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import dev.overgrown.thaumaturge.spell.impl.vinculum.entity.ArcaneMineEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class ModEntities {
    public static final EntityType<SpellBoltEntity> SPELL_BOLT =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Thaumaturge.identifier("spell_bolt"),
                    EntityType.Builder.create(SpellBoltEntity::new, SpawnGroup.MISC)
                            .dimensions(0.5f, 0.5f)
                            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Thaumaturge.identifier("spell_bolt")))
            );

    public static final EntityType<ArcaneMineEntity> ARCANE_MINE = Registry.register(
            Registries.ENTITY_TYPE,
            Thaumaturge.identifier("arcane_mine"),
            EntityType.Builder.create(ArcaneMineEntity::new, SpawnGroup.MISC)
                    .dimensions(0.5f, 0.5f)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Thaumaturge.identifier("arcane_mine")))
    );

    public static final EntityType<MetalShardEntity> METAL_SHARD = Registry.register(
            Registries.ENTITY_TYPE,
            Thaumaturge.identifier("metal_shard"),
            EntityType.Builder.<MetalShardEntity>create(MetalShardEntity::new, SpawnGroup.MISC)
                    .dimensions(0.25f, 0.25f)
                    .maxTrackingRange(4)
                    .trackingTickInterval(10)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Thaumaturge.identifier("metal_shard")))
    );
}