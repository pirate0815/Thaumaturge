package dev.overgrown.thaumaturge.spell.impl.vitium;

import dev.overgrown.aspectslib.corruption.CorruptionAPI;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.tier.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class VitiumEffect implements AspectEffect {

    private static final float BASE_DAMAGE = 3.0f;
    private static final float AOE_RADIUS = 3.0f;
    private static final float CASTER_DAMAGE_CHANCE = 0.1f; // 10% chance
    private static final int VITIUM_CORRUPTION_AMOUNT = 1;

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        float damageAmount = calculateDamage(delivery.getModifiers());
        applyMagicDamage(caster, caster, damageAmount);

        // Add Vitium corruption to biome
        addVitiumCorruption(caster, caster.getBlockPos(), delivery.getModifiers());
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        if (delivery.isEntityTarget() && delivery.getTargetEntity() instanceof LivingEntity target) {
            float damageAmount = calculateDamage(delivery.getModifiers());
            applyMagicDamage(target, delivery.getCaster(), damageAmount);

            // Add Vitium corruption to biome at target location
            addVitiumCorruption(delivery.getCaster(), target.getBlockPos(), delivery.getModifiers());
        } else if (delivery.isBlockTarget()) {
            // Add Vitium corruption to biome at block location
            addVitiumCorruption(delivery.getCaster(), delivery.getBlockPos(), delivery.getModifiers());
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        Vec3d center = Vec3d.ofCenter(delivery.getCenter());
        float damageAmount = calculateDamage(delivery.getModifiers());

        // Get entities in sphere around caster
        Box box = new Box(
                center.getX() - AOE_RADIUS, center.getY() - AOE_RADIUS, center.getZ() - AOE_RADIUS,
                center.getX() + AOE_RADIUS, center.getY() + AOE_RADIUS, center.getZ() + AOE_RADIUS
        );

        List<LivingEntity> entities = delivery.getWorld().getEntitiesByClass(
                LivingEntity.class, box,
                entity -> entity != caster && entity.isAlive()
        );

        // Damage all entities in AOE
        for (LivingEntity entity : entities) {
            applyMagicDamage(entity, caster, damageAmount);
        }

        // 10% chance to damage caster
        if (delivery.getWorld().random.nextFloat() < CASTER_DAMAGE_CHANCE) {
            applyMagicDamage(caster, caster, damageAmount);
        }

        // Add Vitium corruption to biome at center
        addVitiumCorruption(caster, delivery.getCenter(), delivery.getModifiers());
    }

    private float calculateDamage(List<ModifierEffect> modifiers) {
        float damage = BASE_DAMAGE;
        for (ModifierEffect mod : modifiers) {
            if (mod instanceof PowerModifierEffect powerMod) {
                damage *= powerMod.getMultiplier();
            }
        }
        return damage;
    }

    private void applyMagicDamage(LivingEntity target, ServerPlayerEntity source, float amount) {
        DamageSource damageSource = source.getWorld().getDamageSources().create(DamageTypes.MAGIC, source);
        target.damage(damageSource, amount);
    }

    private void addVitiumCorruption(ServerPlayerEntity caster, BlockPos pos, List<ModifierEffect> modifiers) {
        // Calculate corruption amount based on modifiers
        int corruptionAmount = VITIUM_CORRUPTION_AMOUNT;
        for (ModifierEffect mod : modifiers) {
            if (mod instanceof PowerModifierEffect powerMod) {
                corruptionAmount = (int) (corruptionAmount * powerMod.getMultiplier());
            }
        }

        // Add Vitium corruption to the chunk
        if (caster.getWorld() instanceof net.minecraft.server.world.ServerWorld serverWorld) {
            ChunkPos chunkPos = new ChunkPos(pos);
            CorruptionAPI.forceCorruption(serverWorld, chunkPos, corruptionAmount);
        }
    }
}