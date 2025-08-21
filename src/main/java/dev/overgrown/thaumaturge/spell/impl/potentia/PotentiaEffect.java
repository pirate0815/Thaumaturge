package dev.overgrown.thaumaturge.spell.impl.potentia;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.registry.ModEntities;
import dev.overgrown.thaumaturge.registry.ModSounds;
import dev.overgrown.thaumaturge.spell.impl.potentia.entity.SpellBoltEntity;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ScatterModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.spell.utils.SpellHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class PotentiaEffect implements AspectEffect {
    private static final float BASE_DAMAGE = 2.0f;
    private static final float SCATTER_DAMAGE_MULTIPLIER = 0.7f;

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        // Self-cast not implemented for Potentia (yet)
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        World world = delivery.getWorld();

        // Play sound
        world.playSound(
                null,
                caster.getX(), caster.getY(), caster.getZ(),
                ModSounds.POTENTIA_SPELL_CAST,
                SoundCategory.PLAYERS,
                1.0F, 1.0F,
                world.random.nextLong()
        );

        // Get pattern from gauntlet
        ItemStack gauntlet = SpellHandler.findGauntlet(caster);
        if (gauntlet.isEmpty()) return;

        dev.overgrown.thaumaturge.spell.pattern.SpellPattern pattern =
                dev.overgrown.thaumaturge.spell.pattern.SpellPattern.fromGauntlet(gauntlet, "advanced");
        if (pattern == null) return;

        // Get Potentia modifier
        Identifier potentiaId = AspectsLib.identifier("potentia");
        Identifier modifierId = pattern.getAspects().get(potentiaId);
        ModifierEffect modifier = modifierId != null ?
                dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry.get(modifierId) : null;

        // Get carried aspects (excluding Potentia)
        Map<Identifier, Identifier> carriedAspects = new java.util.HashMap<>();
        for (Map.Entry<Identifier, Identifier> entry : pattern.getAspects().entrySet()) {
            if (!entry.getKey().equals(potentiaId)) {
                carriedAspects.put(entry.getKey(), entry.getValue());
            }
        }

        boolean hasCarriedAspects = !carriedAspects.isEmpty();

        // Calculate damage multiplier
        float damageMultiplier = 1.0f;
        if (modifier instanceof PowerModifierEffect powerMod) {
            damageMultiplier = powerMod.getMultiplier();
        }

        // Handle scatter modifier
        if (modifier instanceof ScatterModifierEffect scatterMod) {
            List<Vec3d> directions = scatterMod.scatterAround(
                    caster.getRotationVec(1.0F),
                    world.getRandom()
            );

            for (Vec3d dir : directions) {
                createBolt(
                        caster, world, dir,
                        carriedAspects, hasCarriedAspects,
                        damageMultiplier * SCATTER_DAMAGE_MULTIPLIER
                );
            }
        } else {
            createBolt(
                    caster, world, caster.getRotationVec(1.0F),
                    carriedAspects, hasCarriedAspects,
                    damageMultiplier
            );
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        // AOE not implemented for Potentia (yet)
    }

    private void createBolt(ServerPlayerEntity caster, World world, Vec3d direction,
                            Map<Identifier, Identifier> carriedAspects,
                            boolean hasCarriedAspects, float damageMultiplier) {
        SpellBoltEntity bolt = new SpellBoltEntity(ModEntities.SPELL_BOLT, world);
        bolt.setPosition(caster.getX(), caster.getEyeY(), caster.getZ());
        bolt.setCaster(caster);
        bolt.setVelocity(direction.multiply(1.5));

        // Setup bolt effects
        List<Consumer<Entity>> onHitEffects = new ArrayList<>();
        List<Consumer<BlockHitResult>> onBlockHitEffects = new ArrayList<>();

        if (hasCarriedAspects) {
            // Add effects for carried aspects
            for (Map.Entry<Identifier, Identifier> entry : carriedAspects.entrySet()) {
                AspectEffect effect = AspectRegistry.get(entry.getKey()).orElse(null);
                ModifierEffect modifier = entry.getValue() != null ?
                        dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry.get(entry.getValue()) : null;

                if (effect != null) {
                    onHitEffects.add((entity) -> {
                        if (entity instanceof LivingEntity livingEntity) {
                            TargetedSpellDelivery hitDelivery =
                                    new TargetedSpellDelivery(caster, livingEntity);
                            if (modifier != null) {
                                hitDelivery.setModifiers(java.util.Collections.singletonList(modifier));
                            }
                            effect.applyTargeted(hitDelivery);
                        }
                    });

                    onBlockHitEffects.add((blockHitResult) -> {
                        TargetedSpellDelivery hitDelivery =
                                new TargetedSpellDelivery(caster, blockHitResult.getBlockPos(), blockHitResult.getSide());
                        if (modifier != null) {
                            hitDelivery.setModifiers(java.util.Collections.singletonList(modifier));
                        }
                        effect.applyTargeted(hitDelivery);
                    });
                }
            }
        } else {
            // Default lightning damage
            onHitEffects.add((entity) -> {
                float damage = BASE_DAMAGE * damageMultiplier;
                DamageSource damageSource = world.getDamageSources().create(DamageTypes.LIGHTNING_BOLT, caster);
                entity.damage(damageSource, damage);
            });
        }

        bolt.setOnHitEffects(onHitEffects);
        bolt.setOnBlockHitEffects(onBlockHitEffects);

        world.spawnEntity(bolt);
    }
}