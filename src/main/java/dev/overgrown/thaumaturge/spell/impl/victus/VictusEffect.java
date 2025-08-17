package dev.overgrown.thaumaturge.spell.impl.victus;

import dev.overgrown.thaumaturge.registry.ModSounds;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class VictusEffect implements AspectEffect {
    private static final float BASE_HEALING = 2.0F; // 1 heart
    private static final float BASE_DAMAGE = 2.0F; // 1 heart of damage

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        playSound(caster.getWorld(), caster.getX(), caster.getY(), caster.getZ());
        applyVictusEffect(caster, getPowerMultiplier(delivery.getModifiers()), caster);
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        if (delivery.isEntityTarget() && delivery.getTargetEntity() instanceof LivingEntity target) {
            playSound(delivery.getWorld(), target.getX(), target.getY(), target.getZ());
            applyVictusEffect(target, getPowerMultiplier(delivery.getModifiers()), delivery.getCaster());
        } else if (delivery.isBlockTarget()) {
            BlockPos pos = delivery.getBlockPos();
            if (pos != null) {
                playSound(delivery.getWorld(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            }
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        BlockPos center = delivery.getCenter();
        playSound(delivery.getWorld(), center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5);

        float multiplier = getPowerMultiplier(delivery.getModifiers());
        List<LivingEntity> entities = delivery.getEntitiesInAabb(LivingEntity.class, entity -> true);

        for (LivingEntity entity : entities) {
            applyVictusEffect(entity, multiplier, delivery.getCaster());
        }
    }

    private void playSound(World world, double x, double y, double z) {
        if (world instanceof ServerWorld serverWorld) {
            serverWorld.playSound(
                    null, // No specific player
                    x, y, z,
                    ModSounds.VICTUS_SPELL_CAST,
                    SoundCategory.PLAYERS,
                    1.0F, 1.0F,
                    serverWorld.random.nextLong()
            );
        }
    }

    private float getPowerMultiplier(List<ModifierEffect> modifiers) {
        for (ModifierEffect mod : modifiers) {
            if (mod instanceof PowerModifierEffect powerMod) {
                return powerMod.getMultiplier();
            }
        }
        return 1.0F;
    }

    private void applyVictusEffect(LivingEntity target, float powerMultiplier, ServerPlayerEntity caster) {
        float amount = target.isUndead() ? BASE_DAMAGE * powerMultiplier : BASE_HEALING * powerMultiplier;

        if (target.isUndead()) {
            target.damage(target.getWorld().getDamageSources().magic(), amount);
        } else {
            target.heal(amount);
            if (target.isAlive()) {
                spawnHealParticles((ServerWorld) target.getWorld(), target);
            }
        }
    }

    private void spawnHealParticles(ServerWorld world, LivingEntity entity) {
        double x = entity.getX();
        double y = entity.getY() + entity.getHeight() * 0.5;
        double z = entity.getZ();
        world.spawnParticles(ParticleTypes.HEART, x, y, z, 5, 0.5, 0.5, 0.5, 0.0);
    }
}