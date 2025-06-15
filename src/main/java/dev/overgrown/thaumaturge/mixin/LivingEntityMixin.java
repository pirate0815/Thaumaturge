package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Unique
    private boolean wasFlying = false;

    @Shadow
    protected boolean jumping;

    @Inject(method = "tick", at = @At("HEAD"))
    private void checkFlightStatus(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerEntity player) {
            boolean isFlying = player.hasStatusEffect(Thaumaturge.VOLATUS_FLIGHT_EFFECT);

            if (isFlying && !wasFlying) {
                player.getAbilities().allowFlying = true;
                player.sendAbilitiesUpdate();
            } else if (!isFlying && wasFlying) {
                if (player.getAbilities().flying) {
                    player.getAbilities().flying = false;
                }
                player.getAbilities().allowFlying = false;
                player.sendAbilitiesUpdate();
            }
            wasFlying = isFlying;
        }
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void handleFlightMovement(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self instanceof PlayerEntity player) {
            if (player.hasStatusEffect(Thaumaturge.VOLATUS_FLIGHT_EFFECT)) {
                // Apply air resistance
                player.setVelocity(player.getVelocity().multiply(0.85));

                // Calculate horizontal movement direction
                float yawRadians = (float) Math.toRadians(player.getYaw());
                double inputX = movementInput.x;
                double inputZ = movementInput.z;
                float speed = player.isSprinting() ? 0.5f : 0.25f;

                // Calculate movement vector based on player's rotation
                double motionX = (-MathHelper.sin(yawRadians) * inputZ + MathHelper.cos(yawRadians) * inputX) * speed;
                double motionZ = (MathHelper.cos(yawRadians) * inputZ + MathHelper.sin(yawRadians) * inputX) * speed;

                // Apply horizontal movement
                player.addVelocity(motionX, 0, motionZ);

                // Handle vertical movement
                if (this.jumping) {
                    player.addVelocity(0, speed, 0);
                }
                if (player.isSneaking()) {
                    player.addVelocity(0, -speed, 0);
                }

                player.move(MovementType.SELF, player.getVelocity());
                ci.cancel();
            }
        }
    }
}