package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class FallDamageMixin {

    @Inject(
            at = @At("HEAD"),
            method = "handleFallDamage",
            cancellable = true
    )
    protected void handleFallDamageMixin(
            double fallDistance,
            float damageMultiplier,
            DamageSource damageSource,
            CallbackInfoReturnable<Boolean> cir
    ) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Check for Volatus status effect
        StatusEffectInstance volatusEffect = entity.getStatusEffect(Thaumaturge.VOLATUS_FLIGHT_EFFECT);
        if (volatusEffect != null) {
            // Cancel fall damage completely
            cir.setReturnValue(false);
        }
    }
}