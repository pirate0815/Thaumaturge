package dev.overgrown.thaumaturge.spell.impl.ignis;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;

import java.util.List;

public final class IgnisEffect implements AspectEffect {

    @Override
    public void castOnSelf(ServerPlayerEntity caster, List<ModifierEffect> modifiers) {
        float mult = powerMult(modifiers);
        int durationTicks = (int) (100 * mult); // 5s base → scaled
        caster.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, durationTicks, 0, false, true));
    }

    @Override
    public void castOnEntity(ServerPlayerEntity caster, Entity target, List<ModifierEffect> modifiers) {
        if (target == null || !target.isAlive()) return;
        float mult = powerMult(modifiers);
        int seconds = Math.max(1, Math.round(3 * mult));
        target.setOnFireFor(seconds);
    }

    @Override
    public void castAoe(ServerPlayerEntity caster, BlockPos center, float radius, List<ModifierEffect> modifiers) {
        ServerWorld level = (ServerWorld) caster.getWorld();
        float mult = powerMult(modifiers);

        // Ignite entities in radius
        double cx = center.getX() + 0.5;
        double cy = center.getY() + 0.5;
        double cz = center.getZ() + 0.5;
        Box box = new Box(cx - radius, cy - 2, cz - radius, cx + radius, cy + 2, cz + radius);
        int seconds = Math.max(1, Math.round(2 * mult));

        List<LivingEntity> hits = level.getEntitiesByClass(
                LivingEntity.class,
                box,
                e -> e.isAlive() && e.squaredDistanceTo(cx, cy, cz) <= (radius + 0.5f) * (radius + 0.5f)
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
