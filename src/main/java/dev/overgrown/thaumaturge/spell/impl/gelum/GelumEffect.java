package dev.overgrown.thaumaturge.spell.impl.gelum;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class GelumEffect implements AspectEffect {

    private static final float BASE_DAMAGE = 3.0f;
    private static final int SLOWNESS_DURATION = 100; // 5 seconds
    private static final int FREEZE_RADIUS = 2;
    private static final int FREEZE_DURATION = 200; // 10 seconds

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        // Not implemented for self-cast
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        if (delivery.isEntityTarget() && delivery.getTargetEntity() instanceof LivingEntity target) {
            applyEntityEffect(target, delivery.getCaster(), delivery.getModifiers());
        } else if (delivery.isBlockTarget()) {
            applyBlockEffect(delivery.getWorld(), delivery.getBlockPos(), delivery.getModifiers());
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        // Apply to all entities in AOE
        List<LivingEntity> entities = delivery.getEntitiesInAabb(
                LivingEntity.class,
                entity -> entity.isAlive() && entity != delivery.getCaster()
        );

        for (LivingEntity entity : entities) {
            applyEntityEffect(entity, delivery.getCaster(), delivery.getModifiers());
        }
    }

    private void applyEntityEffect(LivingEntity target, ServerPlayerEntity caster, List<ModifierEffect> modifiers) {
        float damageAmount = calculateDamage(modifiers);

        // Apply freeze damage
        target.damage(target.getWorld().getDamageSources().freeze(), damageAmount);

        // Apply slowness effect
        int amplifier = calculateSlownessAmplifier(modifiers);
        target.addStatusEffect(new StatusEffectInstance(
                StatusEffects.SLOWNESS,
                SLOWNESS_DURATION,
                amplifier,
                false,
                true
        ));
    }

    private void applyBlockEffect(ServerWorld world, BlockPos pos, List<ModifierEffect> modifiers) {
        int radius = calculateFreezeRadius(modifiers);

        // Check all blocks in radius
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos currentPos = pos.add(x, y, z);
                    BlockState state = world.getBlockState(currentPos);

                    // Convert water to frosted ice
                    if (state.getBlock() == Blocks.WATER) {
                        world.setBlockState(currentPos, Blocks.FROSTED_ICE.getDefaultState());

                        // Schedule the frosted ice to melt after duration
                        world.scheduleBlockTick(currentPos, Blocks.FROSTED_ICE, FREEZE_DURATION);
                    }
                }
            }
        }
    }

    private float calculateDamage(List<ModifierEffect> modifiers) {
        float damage = BASE_DAMAGE;
        for (ModifierEffect mod : modifiers) {
            if (mod instanceof PowerModifierEffect powerMod) {
                damage *= powerMod.getMultiplier();
            }
        }
        return damage;
    }

    private int calculateSlownessAmplifier(List<ModifierEffect> modifiers) {
        int amplifier = 0;
        for (ModifierEffect mod : modifiers) {
            if (mod instanceof PowerModifierEffect) {
                amplifier++; // Increase slowness level with power modifier
            }
        }
        return Math.min(amplifier, 3); // Cap at level 3 slowness
    }

    private int calculateFreezeRadius(List<ModifierEffect> modifiers) {
        int radius = FREEZE_RADIUS;
        for (ModifierEffect mod : modifiers) {
            if (mod instanceof PowerModifierEffect powerMod) {
                radius = (int) (radius * powerMod.getMultiplier());
            }
        }
        return Math.min(radius, 5); // Cap radius at 5 blocks
    }
}