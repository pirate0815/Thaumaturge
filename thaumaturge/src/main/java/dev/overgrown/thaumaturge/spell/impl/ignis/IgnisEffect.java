package dev.overgrown.thaumaturge.spell.impl.ignis;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

public final class IgnisEffect implements AspectEffect {

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        // Check for environmental resonance with Aqua (water)
        if (delivery.hasOpposingResonance(AspectsLib.identifier("aqua"))) {
            // In water-rich environment, fire resistance becomes water breathing
            World world = delivery.getCaster().getWorld();
            if (world instanceof ServerWorld serverWorld) {
                createSteamEffect(serverWorld, delivery.getCaster().getBlockPos(), 1.0f);
            }
            delivery.getCaster().addStatusEffect(
                    new StatusEffectInstance(StatusEffects.WATER_BREATHING, 200, 0, false, true)
            );
            return;
        }

        float mult = powerMult(delivery.getModifiers()) * (float) delivery.getAmplificationFactor();
        int durationTicks = (int) (100 * mult); // 5s base → scaled
        delivery.getCaster().addStatusEffect(
                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, durationTicks, 0, false, true)
        );
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        // Check for environmental resonance with Aqua (water)
        if (delivery.hasOpposingResonance(AspectsLib.identifier("aqua"))) {
            // Instead of fire, create steam effect
            BlockPos targetPos = delivery.getBlockPos(); // Default to block position

            if (delivery.isEntityTarget()) {
                Entity targetEntity = delivery.getTargetEntity();
                if (targetEntity != null) {
                    targetPos = targetEntity.getBlockPos();
                }
            }

            createSteamEffect(delivery.getWorld(), targetPos, 1.0f);
            return;
        }

        float mult = powerMult(delivery.getModifiers()) * (float) delivery.getAmplificationFactor();

        if (delivery.isEntityTarget()) {
            Entity entity = delivery.getTargetEntity();
            if (entity != null && entity.isAlive()) {
                int seconds = Math.max(1, Math.round(3 * mult));
                entity.setOnFireFor(seconds);
            }
            return;
        }

        if (delivery.isBlockTarget()) {
            ServerWorld level = delivery.getWorld();
            BlockPos pos = delivery.getBlockPos();
            Direction face = delivery.getFace();
            if (pos != null && face != null) {
                BlockPos place = pos.offset(face);
                tryPlaceFire(level, place, mult);
            }
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        // Check for environmental resonance with Aqua (water)
        if (delivery.hasOpposingResonance(AspectsLib.identifier("aqua"))) {
            // Create steam cloud instead of fire
            createSteamCloud(delivery.getWorld(), delivery.getCenter(), delivery.getRadius());
            return;
        }

        float mult = powerMult(delivery.getModifiers()) * (float) delivery.getAmplificationFactor();
        ServerWorld level = delivery.getWorld();
        BlockPos center = delivery.getCenter();
        float radius = delivery.getRadius();

        // Ignite entities in radius (same logic as original: distance to caster)
        int seconds = Math.max(1, Math.round(2 * mult));
        List<LivingEntity> hits = delivery.getEntitiesInAabb(
                LivingEntity.class,
                e -> e.isAlive() && e.distanceTo(delivery.getCaster()) <= radius + 0.5f
        );
        for (LivingEntity le : hits) {
            le.setOnFireFor(seconds);
        }

        // Light a few fire blocks on nearby air positions (simple ring)
        int r = Math.min(6, Math.max(1, Math.round(radius)));
        int fireCount = 0;
        int maxFires = (int) (5 * mult);

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (fireCount >= maxFires) break;
                if (dx * dx + dz * dz > r * r) continue;
                BlockPos p = center.add(dx, 0, dz);
                // try ground or just above ground
                BlockPos ground = findGround(level, p);
                if (ground != null) {
                    if (tryPlaceFire(level, ground.up(), mult)) {
                        fireCount++;
                    }
                }
            }
        }
    }

    private static float powerMult(List<ModifierEffect> mods) {
        if (mods == null) return 1.0f;
        float multiplier = 1.0f;
        for (ModifierEffect m : mods) {
            if (m instanceof PowerModifierEffect p) {
                multiplier = Math.max(0.1f, p.getMultiplier());
                break; // Use the first power modifier found
            }
        }
        return multiplier;
    }

    private static boolean tryPlaceFire(ServerWorld level, BlockPos pos, float chanceMultiplier) {
        if (!level.isInBuildLimit(pos)) return false;
        BlockState fire = Blocks.FIRE.getDefaultState();
        if (level.isAir(pos) && fire.canPlaceAt(level, pos)) {
            // Use chance multiplier to determine if fire should be placed
            if (level.random.nextFloat() < (0.7f * chanceMultiplier)) {
                level.setBlockState(pos, fire, 11);
                return true;
            }
        }
        return false;
    }

    private static BlockPos findGround(ServerWorld level, BlockPos start) {
        BlockPos.Mutable m = start.mutableCopy();
        // search down a bit for solid surface
        for (int i = 0; i < 4; i++) {
            if (!level.isAir(m) && level.getBlockState(m).isSolidBlock(level, m)) {
                return m.toImmutable();
            }
            m.move(Direction.DOWN);
        }
        return null;
    }

    /**
     * Creates steam particles and sound effect
     */
    private void createSteamEffect(ServerWorld world, BlockPos pos, float intensity) {
        if (world == null) return;

        // Spawn campfire smoke particles (steam-like)
        world.spawnParticles(
                ParticleTypes.CAMPFIRE_COSY_SMOKE,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                (int) (10 * intensity),
                0.5, 0.3, 0.5,
                0.1
        );

        // Also add some white particles for better steam effect
        world.spawnParticles(
                ParticleTypes.CLOUD,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                (int) (5 * intensity),
                0.3, 0.2, 0.3,
                0.05
        );

        // Play steam/fire extinguishing sound
        world.playSound(
                null,
                pos,
                SoundEvents.BLOCK_FIRE_EXTINGUISH,
                SoundCategory.BLOCKS,
                0.5f * intensity,
                1.0f + (world.random.nextFloat() - 0.5f) * 0.2f
        );
    }

    /**
     * Creates a steam cloud in an area
     */
    private void createSteamCloud(ServerWorld world, BlockPos center, float radius) {
        int particleCount = (int) (30 * radius);

        for (int i = 0; i < particleCount; i++) {
            double x = center.getX() + 0.5 + (world.random.nextDouble() - 0.5) * radius * 2;
            double y = center.getY() + 1 + (world.random.nextDouble() - 0.5) * radius;
            double z = center.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * radius * 2;

            world.spawnParticles(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    x, y, z,
                    1, 0, 0, 0, 0.1
            );

            // Every 3rd particle is a cloud particle for variety
            if (i % 3 == 0) {
                world.spawnParticles(
                        ParticleTypes.CLOUD,
                        x, y, z,
                        1, 0.1, 0.1, 0.1, 0.05
                );
            }
        }

        // Play multiple extinguishing sounds around the area
        for (int i = 0; i < 3; i++) {
            double x = center.getX() + 0.5 + (world.random.nextDouble() - 0.5) * radius;
            double z = center.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * radius;
            BlockPos soundPos = new BlockPos((int)x, center.getY(), (int)z);

            world.playSound(
                    null,
                    soundPos,
                    SoundEvents.BLOCK_FIRE_EXTINGUISH,
                    SoundCategory.BLOCKS,
                    0.3f,
                    0.8f + world.random.nextFloat() * 0.4f
            );
        }
    }
}