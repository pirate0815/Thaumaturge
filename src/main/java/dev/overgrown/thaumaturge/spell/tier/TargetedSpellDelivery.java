package dev.overgrown.thaumaturge.spell.tier;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TargetedSpellDelivery {
    private int projectileCount = 1;
    private float spread = 0.0f;
    private double maxDistance = 16.0;
    private ServerPlayerEntity caster;
    private boolean swapActorTarget = false;
    private final List<Consumer<Entity>> onHitEffects = new ArrayList<>();

    public void setProjectileCount(int count) {
        this.projectileCount = count;

    }
    public void setSpread(float spread) {
        this.spread = spread;
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
    }

    public ServerPlayerEntity getCaster() {
        return caster;
    }

    public void setSwapActorTarget(boolean swap) {
        this.swapActorTarget = swap;
    }

    public void addOnHitEffect(Consumer<Entity> effect) {
        onHitEffects.add(effect);
    }

    public List<Consumer<Entity>> getOnHitEffects() {
        return onHitEffects;
    }

    public void execute(ServerPlayerEntity caster) {
        this.caster = caster;
        World world = caster.getWorld();
        Vec3d eyePos = caster.getEyePos();

        for (int i = 0; i < projectileCount; i++) {
            // Calculate spread direction
            float yawOffset = (i - (projectileCount - 1) / 2.0f) * spread;
            float currentYaw = caster.getYaw() + yawOffset;
            float currentPitch = caster.getPitch();

            Vec3d direction = Vec3d.fromPolar(currentPitch, currentYaw).normalize();
            Vec3d endPos = eyePos.add(direction.multiply(maxDistance));

            // Perform ray-cast to find the first entity in the path
            EntityHitResult hit = ProjectileUtil.raycast(
                    caster,
                    eyePos,
                    endPos,
                    new Box(eyePos, endPos),
                    entity -> !entity.isSpectator() && entity.isAlive() && entity != caster,
                    maxDistance
            );

            // Apply effects if an entity is hit
            if (hit != null) {
                Entity target = hit.getEntity();
                if (swapActorTarget) {
                    onHitEffects.forEach(effect -> effect.accept(caster));
                } else {
                    onHitEffects.forEach(effect -> effect.accept(target));
                }
            }
        }
    }
}