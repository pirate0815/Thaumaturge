package dev.overgrown.thaumaturge.spell.impl.motus;

import dev.overgrown.thaumaturge.registry.ModSounds;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class MotusEffect implements AspectEffect {
    private static final float BASE_PUSH_STRENGTH = 1.5f;
    private static final float AOE_PUSH_STRENGTH = 1.0f;
    private static final float SELF_REACTION_CHANCE = 0.1f; // 10% chance for self-reaction in AOE

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        playSound(caster.getWorld(), caster.getX(), caster.getY(), caster.getZ());

        // Get look direction
        Vec3d lookDirection = caster.getRotationVector();

        // Remove vertical component to prevent flying
        Vec3d horizontalDirection = new Vec3d(lookDirection.x, 0, lookDirection.z).normalize();

        applyVelocity(caster, horizontalDirection, getPowerMultiplier(delivery.getModifiers()), BASE_PUSH_STRENGTH);
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        if (delivery.isEntityTarget()) {
            Entity target = delivery.getTargetEntity();
            if (target == null) return;

            playSound(delivery.getWorld(), target.getX(), target.getY(), target.getZ());

            ServerPlayerEntity caster = delivery.getCaster();
            if (caster == null) return;

            Vec3d pushDirection = target.getPos()
                    .subtract(caster.getPos())
                    .normalize();

            applyVelocity(target, pushDirection, getPowerMultiplier(delivery.getModifiers()), BASE_PUSH_STRENGTH);
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        BlockPos center = delivery.getCenter();
        playSound(delivery.getWorld(), center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);

        float powerMultiplier = getPowerMultiplier(delivery.getModifiers());
        List<LivingEntity> entities = delivery.getEntitiesInAabb(
                LivingEntity.class,
                e -> e.isAlive() && e != caster
        );

        // Apply to other entities
        for (LivingEntity entity : entities) {
            Vec3d pushDirection = entity.getPos()
                    .subtract(caster.getPos())
                    .normalize();

            applyVelocity(entity, pushDirection, powerMultiplier, AOE_PUSH_STRENGTH);
        }

        // Small chance for caster self-reaction
        if (new Random().nextFloat() < SELF_REACTION_CHANCE) {
            Vec3d reverseDirection = caster.getRotationVector().multiply(-1);
            applyVelocity(caster, reverseDirection, powerMultiplier, AOE_PUSH_STRENGTH);
        }
    }

    private void playSound(World world, double x, double y, double z) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(
                    null,
                    x, y, z,
                    ModSounds.MOTUS_SPELL_CAST,
                    SoundCategory.PLAYERS,
                    1.0F, 1.0F,
                    serverWorld.random.nextLong()
            );
        }
    }

    private void applyVelocity(Entity entity, Vec3d direction, float powerMultiplier, float baseStrength) {
        Vec3d velocity = direction.multiply(baseStrength * powerMultiplier);
        entity.addVelocity(velocity.x, velocity.y, velocity.z);
        entity.velocityModified = true;
    }

    private float getPowerMultiplier(List<ModifierEffect> modifiers) {
        for (ModifierEffect mod : modifiers) {
            if (mod instanceof PowerModifierEffect powerMod) {
                return powerMod.getMultiplier();
            }
        }
        return 1.0f;
    }
}