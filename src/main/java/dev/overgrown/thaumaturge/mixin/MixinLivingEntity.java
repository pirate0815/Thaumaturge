package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.effect.ModStatusEffects;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity {

    public MixinLivingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Shadow
    protected abstract void playBlockFallSound();

    @Shadow
    protected abstract SoundEvent getFallSound(int distance);

    @Shadow protected abstract void travelMidAir(Vec3d movementInput);

    @Unique
    protected abstract int computeFallDamage(float fallDistance, float damageMultiplier);

    @Redirect(
            method = "travelMidAir",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F")
    )
    private float modifySlipperiness(Block instance) {
        if (((LivingEntity)(Object)this).hasStatusEffect(ModStatusEffects.ICY_FRICTION)) {
            return 1.09F;
        }
        return ((AbstractBlockAccessor) instance).getSlipperiness();
    }

    @Inject(method = "travelMidAir", at = @At("TAIL"))
    private void onTravel(Vec3d movementInput, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity)(Object)this;
        if (entity.hasStatusEffect(ModStatusEffects.ICY_FRICTION) && entity.getWorld().isClient()) {
            World world = entity.getWorld();
            for (int i = 0; i < 4; i++) {
                double x = entity.getX() + (world.random.nextDouble() - 0.5) * entity.getWidth();
                double y = entity.getY() + 0.1;
                double z = entity.getZ() + (world.random.nextDouble() - 0.5) * entity.getWidth();
                world.addParticleClient(ParticleTypes.SNOWFLAKE, x, y, z, 0, 0, 0);
            }
        }
    }
}