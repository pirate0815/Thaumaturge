package dev.overgrown.aspectslib.mixin.spell;

import dev.overgrown.aspectslib.spell.aether.PersonalAetherPool;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects a Personal Aether pool onto every {@link LivingEntity}.
 *
 * <p>Storage uses two {@link TrackedData} fields (current + max) so that the
 * values are automatically synced to tracking clients.  NBT serialisation
 * ensures the values persist across server restarts.
 *
 * <p>Default pool sizes on first spawn:
 * <ul>
 *   <li>Players: 100 (Apprentice baseline; consuming mods should raise this)</li>
 *   <li>All other LivingEntities: 20 (untrained/creature baseline)</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityAetherMixin extends Entity implements PersonalAetherPool {

    @Unique
    private static final TrackedData<Float> PERSONAL_AETHER =
            DataTracker.registerData(LivingEntityAetherMixin.class,
                    TrackedDataHandlerRegistry.FLOAT);

    @Unique
    private static final TrackedData<Float> MAX_PERSONAL_AETHER =
            DataTracker.registerData(LivingEntityAetherMixin.class,
                    TrackedDataHandlerRegistry.FLOAT);

    public LivingEntityAetherMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    // DataTracker registration
    @Inject(
            method = "initDataTracker",
            at = @At(
                    "TAIL"
            )
    )
    private void aspectslib$initAetherTrackers(CallbackInfo ci) {
        float defaultMax = ((LivingEntity)(Object)this) instanceof net.minecraft.entity.player.PlayerEntity
                ? 100f : 20f;
        this.dataTracker.startTracking(PERSONAL_AETHER, defaultMax);
        this.dataTracker.startTracking(MAX_PERSONAL_AETHER, defaultMax);
    }

    // NBT persistence

    @Inject(
            method = "writeCustomDataToNbt",
            at = @At(
                    "TAIL"
            )
    )
    private void aspectslib$writeAetherNbt(NbtCompound nbt, CallbackInfo ci) {
        nbt.putFloat("PersonalAether", this.dataTracker.get(PERSONAL_AETHER));
        nbt.putFloat("MaxPersonalAether", this.dataTracker.get(MAX_PERSONAL_AETHER));
    }

    @Inject(
            method = "readCustomDataFromNbt",
            at = @At(
                    "TAIL"
            )
    )
    private void aspectslib$readAetherNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("PersonalAether")) this.dataTracker.set(PERSONAL_AETHER, nbt.getFloat("PersonalAether"));
        if (nbt.contains("MaxPersonalAether")) this.dataTracker.set(MAX_PERSONAL_AETHER, nbt.getFloat("MaxPersonalAether"));
    }

    // PersonalAetherPool implementation
    @Override
    @Unique
    public double aspectslib$getPersonalAether() {
        return this.dataTracker.get(PERSONAL_AETHER);
    }

    @Override
    @Unique
    public void aspectslib$setPersonalAether(double amount) {
        double max = aspectslib$getMaxPersonalAether();
        this.dataTracker.set(PERSONAL_AETHER, (float) Math.max(0.0, Math.min(max, amount)));
    }

    @Override
    @Unique
    public double aspectslib$getMaxPersonalAether() {
        return this.dataTracker.get(MAX_PERSONAL_AETHER);
    }

    @Override
    @Unique
    public void aspectslib$setMaxPersonalAether(double max) {
        float clamped = (float) Math.max(0.0, max);
        this.dataTracker.set(MAX_PERSONAL_AETHER, clamped);
        // If current exceeds new max, clamp it down
        if (this.dataTracker.get(PERSONAL_AETHER) > clamped) {
            this.dataTracker.set(PERSONAL_AETHER, clamped);
        }
    }
}