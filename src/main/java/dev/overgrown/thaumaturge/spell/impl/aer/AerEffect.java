package dev.overgrown.thaumaturge.spell.impl.aer;

import dev.overgrown.thaumaturge.registry.ModSounds;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.Random;

public class AerEffect implements AspectEffect {
    private static final float BASE_SELF_STRENGTH = 0.8f; // Enough to clear 3 blocks
    private static final float BASE_TARGETED_STRENGTH = 0.8f;
    private static final float BASE_AOE_STRENGTH = 0.8f;
    private static final float SELF_REACTION_CHANCE = 0.1f; // 10% chance for self-reaction in AOE

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        if (caster.getWorld() instanceof ServerWorld serverWorld) {
            playEffects(serverWorld, caster.getX(), caster.getY(), caster.getZ());
        }

        float strength = calculateStrength(BASE_SELF_STRENGTH, delivery.getModifiers());
        applyUpwardVelocity(caster, strength);
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        if (delivery.isEntityTarget()) {
            Entity target = delivery.getTargetEntity();
            if (target == null) return;

            playEffects(delivery.getWorld(), target.getX(), target.getY(), target.getZ());

            float strength = calculateStrength(BASE_TARGETED_STRENGTH, delivery.getModifiers());
            applyUpwardVelocity(target, strength);
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        playEffects(delivery.getWorld(), delivery.getCenter().getX() + 0.5, delivery.getCenter().getY(), delivery.getCenter().getZ() + 0.5);

        float strength = calculateStrength(BASE_AOE_STRENGTH, delivery.getModifiers());

        // Apply to all entities in AOE
        List<Entity> entities = delivery.getEntitiesInAabb(
                Entity.class,
                entity -> entity.isAlive() && entity != caster
        );

        for (Entity entity : entities) {
            applyUpwardVelocity(entity, strength);
        }

        // 10% chance to also apply to caster
        if (new Random().nextFloat() < SELF_REACTION_CHANCE) {
            applyUpwardVelocity(caster, strength);
        }
    }

    private void applyUpwardVelocity(Entity entity, float strength) {
        Vec3d currentVelocity = entity.getVelocity();
        entity.setVelocity(currentVelocity.x, strength, currentVelocity.z);
        entity.velocityModified = true;
    }

    private float calculateStrength(float baseStrength, List<ModifierEffect> modifiers) {
        float multiplier = 1.0f;
        for (ModifierEffect mod : modifiers) {
            if (mod instanceof PowerModifierEffect powerMod) {
                multiplier = powerMod.getMultiplier();
                break;
            }
        }
        return baseStrength * multiplier;
    }

    private void playEffects(ServerWorld world, double x, double y, double z) {
        // Play sound
        world.playSound(
                null,
                x, y, z,
                ModSounds.AER_SPELL_CAST,
                SoundCategory.PLAYERS,
                1.0F, 1.0F,
                world.random.nextLong()
        );

        // Spawn poof particles at feet
        world.spawnParticles(
                ParticleTypes.POOF,
                x, y, z,
                10, // Count
                0.5, 0.1, 0.5, // Delta
                0.05 // Speed
        );
    }
}