package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.effect.SpellEffect;
import dev.overgrown.thaumaturge.spell.utils.SpellContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import java.util.List;
import java.util.function.Predicate;

public class TargetedSpellDelivery implements SpellDelivery {
    @Override
    public void cast(World world, PlayerEntity caster, List<SpellEffect> effects) {
        Vec3d start = caster.getCameraPosVec(1.0F);
        Vec3d rotationVec = caster.getRotationVec(1.0F);
        Vec3d end = start.add(rotationVec.multiply(32));

        // Create a bounding box for entity ray-casting
        Box box = caster.getBoundingBox().stretch(rotationVec.multiply(32)).expand(1.0);
        Predicate<Entity> predicate = entity ->
                !entity.isSpectator() && entity.isCollidable();

        // Raycast for entities
        EntityHitResult entityHit = ProjectileUtil.raycast(
                caster, start, end, box, predicate, 32.0 * 32.0
        );

        // Use block raycast as fallback
        HitResult hit = world.raycast(new RaycastContext(
                start, end,
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.NONE,
                caster
        ));

        // Prefer entity hit if available
        if (entityHit != null) {
            hit = entityHit;
        }

        SpellContext context = createContextFromHit(world, caster, hit);

        for (SpellEffect effect : effects) {
            effect.apply(context);
        }
    }

    private SpellContext createContextFromHit(World world, PlayerEntity caster, HitResult hit) {
        if (hit == null) {
            return new SpellContext(world, caster, null, null);
        }

        return switch (hit.getType()) {
            case ENTITY -> {
                Entity target = ((EntityHitResult) hit).getEntity();
                yield new SpellContext(world, caster, target, target.getBlockPos());
            }
            case BLOCK -> {
                BlockPos pos = ((BlockHitResult) hit).getBlockPos();
                yield new SpellContext(world, caster, null, pos);
            }
            default -> // MISS
                    new SpellContext(world, caster, null, null);
        };
    }
}