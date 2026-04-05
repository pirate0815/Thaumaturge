package dev.overgrown.aspectslib.mixin.spell;

import dev.overgrown.aspectslib.spell.unraveling.UnravelingTracker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Persists Unraveling stress values to/from NBT so they survive server restarts.
 * The in-memory map in {@link UnravelingTracker} is authoritative at runtime;
 * this mixin only handles disk persistence.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityUnravelingMixin extends Entity {

    public LivingEntityUnravelingMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At(
                    "TAIL"
            )
    )
    private void aspectslib$writeUnravelingNbt(NbtCompound nbt, CallbackInfo ci) {
        UnravelingTracker.writeNbt((LivingEntity)(Object)this, nbt);
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At(
                    "TAIL"
            )
    )
    private void aspectslib$readUnravelingNbt(NbtCompound nbt, CallbackInfo ci) {
        UnravelingTracker.readNbt((LivingEntity)(Object)this, nbt);
    }
}