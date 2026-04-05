package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import dev.overgrown.thaumaturge.spell.impl.vinculum.entity.ArcaneMineEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModEntities {
    public static final EntityType<SpellBoltEntity> SPELL_BOLT =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Thaumaturge.identifier("spell_bolt"),
                    EntityType.Builder.create(SpellBoltEntity::new, SpawnGroup.MISC)
                            .setDimensions(0.5f, 0.5f)
                            .disableSaving()
                            .build(Thaumaturge.identifier("spell_bolt").toString())
            );

    public static final EntityType<ArcaneMineEntity> ARCANE_MINE =
            Registry.register(
                    Registries.ENTITY_TYPE,
                    Thaumaturge.identifier("arcane_mine"),
                    EntityType.Builder.<ArcaneMineEntity>create(ArcaneMineEntity::new, SpawnGroup.MISC)
                            .setDimensions(0.5f, 0.1f) // Flat mine shape
                            .disableSaving()
                            .build(Thaumaturge.identifier("arcane_mine").toString())
            );
}