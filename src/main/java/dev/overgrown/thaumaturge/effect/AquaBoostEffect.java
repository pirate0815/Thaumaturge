package dev.overgrown.thaumaturge.effect;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

public class AquaBoostEffect extends StatusEffect {
    public static final Identifier SWIM_SPEED_ID = Thaumaturge.identifier("aqua_boost_swim_speed");
    public static final Identifier OXYGEN_BONUS_ID = Thaumaturge.identifier("aqua_boost_oxygen");

    public AquaBoostEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x33CCFF);
        this.addAttributeModifier(EntityAttributes.OXYGEN_BONUS, OXYGEN_BONUS_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE);
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        boolean inWater = entity.isSubmergedInWater();
        EntityAttributeInstance waterMovement = entity.getAttributeInstance(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);

        // Use the same ID for the modifier
        EntityAttributeModifier speedModifier = new EntityAttributeModifier(
                SWIM_SPEED_ID, 0.96F * (amplifier + 1), EntityAttributeModifier.Operation.ADD_VALUE
        );

        EntityAttributeModifier oxygenModifier = new EntityAttributeModifier(
                OXYGEN_BONUS_ID, 10.0, EntityAttributeModifier.Operation.ADD_VALUE
        );

        if (inWater) {
            // Check if the modifier ID exists, not the modifier object
            if (waterMovement != null && !waterMovement.hasModifier(SWIM_SPEED_ID)) {
                waterMovement.addTemporaryModifier(speedModifier);
            }
            EntityAttributeInstance oxygen = entity.getAttributeInstance(EntityAttributes.OXYGEN_BONUS);
            if (oxygen != null && !oxygen.hasModifier(OXYGEN_BONUS_ID)) {
                oxygen.addTemporaryModifier(oxygenModifier);
            }
        } else {
            // Remove by ID
            if (waterMovement != null) {
                waterMovement.removeModifier(SWIM_SPEED_ID);
            }
            EntityAttributeInstance oxygen = entity.getAttributeInstance(EntityAttributes.OXYGEN_BONUS);
            if (oxygen != null) {
                oxygen.removeModifier(OXYGEN_BONUS_ID);
            }
        }
        return true;
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true; // Apply every tick
    }

    @Override
    public void onRemoved(AttributeContainer attributes) {
        super.onRemoved(attributes);
        EntityAttributeInstance waterMovement = attributes.getCustomInstance(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);
        if (waterMovement != null) {
            waterMovement.removeModifier(SWIM_SPEED_ID);
        }

        EntityAttributeInstance oxygen = attributes.getCustomInstance(EntityAttributes.OXYGEN_BONUS);
        if (oxygen != null) {
            oxygen.removeModifier(OXYGEN_BONUS_ID);
        }
    }
}