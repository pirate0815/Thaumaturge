package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SelfSpellDelivery {
    private final SpellCastPacket.SpellTier tier;
    private float powerMultiplier = 1.0f;
    private final List<Consumer<Entity>> effects = new ArrayList<>();
    private boolean redirectToTarget = false;
    private int scatterSize = 0;

    public SelfSpellDelivery(SpellCastPacket.SpellTier tier) {
        this.tier = tier;
    }

    public List<Consumer<Entity>> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    public void setRedirectToTarget(boolean redirect) {
        this.redirectToTarget = redirect;
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

    public void addEffect(Consumer<Entity> effect) {
        effects.add(effect);
    }

    public int getScatterSize() {
        return scatterSize;
    }

    public void setScatterSize(int scatterSize) {
        this.scatterSize = scatterSize;
    }

    public void execute(ServerPlayerEntity caster) {
        if (redirectToTarget) {
            // Perform raycast to find target entity
            World world = caster.getWorld();
            Vec3d eyePos = caster.getEyePos();
            Vec3d direction = caster.getRotationVector().normalize();
            Vec3d endPos = eyePos.add(direction.multiply(16.0));

            EntityHitResult entityHit = ProjectileUtil.raycast(
                    caster,
                    eyePos,
                    endPos,
                    new Box(eyePos, endPos),
                    entity -> !entity.isSpectator() && entity.isAlive() && entity != caster,
                    16.0
            );

            if (entityHit != null) {
                Entity target = entityHit.getEntity();
                effects.forEach(effect -> effect.accept(target)); // Apply to target
            }
        } else {
            effects.forEach(effect -> effect.accept(caster)); // Apply to caster
        }
    }
}