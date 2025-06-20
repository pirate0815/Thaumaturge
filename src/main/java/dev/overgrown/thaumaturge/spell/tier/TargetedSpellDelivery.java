package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TargetedSpellDelivery {
    private int projectileCount = 1;
    private float spread = 0.0f;
    private double maxDistance = 16.0;
    private ServerPlayerEntity caster;
    private boolean swapActorTarget = false;
    private final SpellCastPacket.SpellTier tier;
    private float powerMultiplier = 1.0f;
    private final List<Consumer<Entity>> onHitEffects = new ArrayList<>();
    private final List<Consumer<BlockHitResult>> onBlockHitEffects = new ArrayList<>();
    private int scatterSize = 0;

    public int getScatterSize() {
        return scatterSize;
    }

    public void setScatterSize(int scatterSize) {
        this.scatterSize = scatterSize;
    }

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

    public void setCaster(ServerPlayerEntity caster) {
        this.caster = caster;
    }

    public void setSwapActorTarget(boolean swap) {
        this.swapActorTarget = swap;
    }

    public void addOnHitEffect(Consumer<Entity> effect) {
        onHitEffects.add(effect);
    }

    public TargetedSpellDelivery(SpellCastPacket.SpellTier tier) {
        this.tier = tier;
    }

    public SpellCastPacket.SpellTier getTier() {
        return tier;
    }

    public float getPowerMultiplier() {
        return powerMultiplier;
    }

    public void setPowerMultiplier(float powerMultiplier) {
        this.powerMultiplier = powerMultiplier;
    }

    public List<Consumer<Entity>> getOnHitEffects() {
        return onHitEffects;
    }

    public List<Consumer<BlockHitResult>> getOnBlockHitEffects() {
        return onBlockHitEffects;
    }

    public void addBlockHitEffect(Consumer<BlockHitResult> effect) {
        onBlockHitEffects.add(effect);
    }

    public ServerWorld getWorld() {
        return caster.getWorld();
    }

    public void execute(ServerPlayerEntity caster) {
        this.caster = caster;
        ServerWorld world = getWorld();
        Vec3d eyePos = caster.getEyePos();

        for (int i = 0; i < projectileCount; i++) {
            // Calculate spread direction
            float yawOffset = (i - (projectileCount - 1) / 2.0f) * spread;
            float currentYaw = caster.getYaw() + yawOffset;
            float currentPitch = caster.getPitch();

            Vec3d direction = Vec3d.fromPolar(currentPitch, currentYaw).normalize();
            Vec3d endPos = eyePos.add(direction.multiply(maxDistance));

            // Perform ray-cast to find the first entity in the path
            EntityHitResult entityHit = ProjectileUtil.raycast(
                    caster,
                    eyePos,
                    endPos,
                    new Box(eyePos, endPos),
                    entity -> !entity.isSpectator() && entity.isAlive() && entity != caster,
                    maxDistance
            );

            // Apply effects if an entity is hit
            if (entityHit != null) {
                Entity target = swapActorTarget ? caster : entityHit.getEntity();
                for (Consumer<Entity> effect : onHitEffects) {
                    effect.accept(target);
                }
            } else {
                // Check for block hit
                RaycastContext raycastContext = new RaycastContext(
                        eyePos, endPos,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.ANY,
                        caster
                );
                BlockHitResult blockHit = world.raycast(raycastContext);
                if (blockHit != null && blockHit.getType() == HitResult.Type.BLOCK) {
                    for (Consumer<BlockHitResult> effect : onBlockHitEffects) {
                        effect.accept(blockHit);
                    }
                }
            }
        }
    }
}