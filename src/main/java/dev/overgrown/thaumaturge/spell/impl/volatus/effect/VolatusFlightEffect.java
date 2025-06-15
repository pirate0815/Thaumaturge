package dev.overgrown.thaumaturge.spell.impl.volatus.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;

public class VolatusFlightEffect extends StatusEffect {
    public VolatusFlightEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x87CEEB); // Sky blue color
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    public void applyUpdateEffect(LivingEntity entity, int amplifier) {
        // Flight logic handled in mixins (see LivingEntityMixin)
        // Empty body is intentional - no operation needed here
    }
}