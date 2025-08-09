package dev.overgrown.thaumaturge.spell.impl.ignis;

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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public final class IgnisEffect implements AspectEffect {

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        float mult = powerMult(delivery.getModifiers());
        int durationTicks = (int) (100 * mult); // 5s base → scaled
        delivery.getCaster().addStatusEffect(
                new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, durationTicks, 0, false, true)
        );
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        float mult = powerMult(delivery.getModifiers());

        if (delivery.isEntityTarget()) {
            Entity e = delivery.getTargetEntity();
            if (e != null && e.isAlive()) {
                int seconds = Math.max(1, Math.round(3 * mult));
                e.setOnFireFor(seconds);
            }
            return;
        }

        if (delivery.isBlockTarget()) {
            ServerWorld level = delivery.getWorld();
            BlockPos pos = delivery.getBlockPos();
            Direction face = delivery.getFace();
            if (pos != null && face != null) {
                BlockPos place = pos.offset(face);
                tryPlaceFire(level, place);
            }
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        float mult = powerMult(delivery.getModifiers());
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
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (dx * dx + dz * dz > r * r) continue;
                BlockPos p = center.add(dx, 0, dz);
                // try ground or just above ground
                BlockPos ground = findGround(level, p);
                if (ground != null) {
                    tryPlaceFire(level, ground.up());
                }
            }
        }
    }

    private static float powerMult(List<ModifierEffect> mods) {
        if (mods == null) return 1.0f;
        for (ModifierEffect m : mods) {
            if (m instanceof PowerModifierEffect p) {
                return Math.max(0.1f, p.getMultiplier());
            }
        }
        return 1.0f;
    }

    private static void tryPlaceFire(ServerWorld level, BlockPos pos) {
        if (!level.isInBuildLimit(pos)) return;
        BlockState fire = Blocks.FIRE.getDefaultState();
        if (level.isAir(pos) && fire.canPlaceAt(level, pos)) {
            level.setBlockState(pos, fire, 11);
        }
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
}
