package dev.overgrown.aspectslib.mixin.spell;

import dev.overgrown.aspectslib.spell.perception.AetherSightCapability;
import dev.overgrown.aspectslib.spell.perception.AetherSightData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects {@link AetherSightCapability} onto every {@link LivingEntity}.
 * Sight data is persisted via NBT so practitioner progress survives restarts.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntitySightMixin extends Entity implements AetherSightCapability {

    @Unique
    private AetherSightData aspectslib$sightData = new AetherSightData();

    public LivingEntitySightMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // NBT persistence
    @Inject(
            method = "writeCustomDataToNbt",
            at = @At(
                    "TAIL"
            )
    )
    private void aspectslib$writeSightNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.put("AetherSight", aspectslib$sightData.toNbt());
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At(
                    "TAIL"
            )
    )
    private void aspectslib$readSightNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("AetherSight")) {
            aspectslib$sightData = AetherSightData.fromNbt(nbt.getCompound("AetherSight"));
        }
    }

    // AetherSightCapability implementation
    @Override
    @Unique
    public AetherSightData aspectslib$getSightData() {
        return aspectslib$sightData;
    }
}