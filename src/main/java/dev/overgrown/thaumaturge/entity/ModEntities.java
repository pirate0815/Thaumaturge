package dev.overgrown.thaumaturge.entity;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModEntities {
    public static final EntityType<SpellBoltEntity> SPELL_BOLT =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Thaumaturge.identifier("spell_bolt"),
                    EntityType.Builder.<SpellBoltEntity>create(SpellBoltEntity::new, SpawnGroup.MISC)
                            .dimensions(0.5f, 0.5f)
                            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Thaumaturge.identifier("spell_bolt")))
            );
}