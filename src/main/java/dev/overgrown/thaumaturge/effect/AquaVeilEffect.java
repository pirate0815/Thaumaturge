package dev.overgrown.thaumaturge.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;

public class AquaVeilEffect extends StatusEffect {
    public AquaVeilEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x00AAFF);
    }

    @Override
    public void onApplied(AttributeContainer attributes, int amplifier) {
        super.onApplied(attributes, amplifier);
        // Add any attribute modifications here if needed
    }

    @Override
    public void onEntityRemoval(ServerWorld world, LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
        super.onEntityRemoval(world, entity, amplifier, reason);
        if (reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED) {
            // Apply Dehydrated effect when Aqua Veil is removed
            entity.addStatusEffect(new StatusEffectInstance(ModStatusEffects.DEHYDRATED, 30 * 20));
        }
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        // Optional: Apply effects each tick if needed
        return false;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // If effect needs to apply updates every tick
    }

    // Handle damage modification via onEntityDamage
    @Override
    public void onEntityDamage(ServerWorld world, LivingEntity entity, int amplifier, DamageSource source, float amount) {
        if (source.isIn(DamageTypeTags.IS_FIRE)) {
            // Example: Reduce fire damage by 25%
            float reducedAmount = amount * 0.75f;
            // Apply the reduced damage (this part is illustrative; actual implementation may vary)
            entity.damage(world, source, reducedAmount);
            return; // Prevent further processing if handled
        }
        super.onEntityDamage(world, entity, amplifier, source, amount);
    }
}