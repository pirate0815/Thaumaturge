package dev.overgrown.thaumaturge.spell.impl.potentia.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpellBoltEntity extends ThrownEntity {

    @Nullable
    private PlayerEntity caster;
    private List<Consumer<Entity>> onHitEffects = new ArrayList<>();
    private static final TrackedData<Integer> CASTER_ID = DataTracker.registerData(SpellBoltEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> TIER = DataTracker.registerData(SpellBoltEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Long> SEED = DataTracker.registerData(SpellBoltEntity.class, TrackedDataHandlerRegistry.LONG);
    private static final TrackedData<Boolean> HIT_TARGET = DataTracker.registerData(SpellBoltEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private List<Consumer<BlockHitResult>> onBlockHitEffects = new ArrayList<>();
    private int despawnTimer = 10;
    private Vec3d direction; // Store the initial direction

    public SpellBoltEntity(EntityType<? extends ThrownEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true); // Disable gravity
    }

    public void setCaster(PlayerEntity caster) {
        this.dataTracker.set(CASTER_ID, caster.getId());
        this.caster = caster;
    }

    @Nullable
    public PlayerEntity getCaster() {
        if (this.caster == null && this.getWorld() != null && this.getWorld().getEntityById(this.dataTracker.get(CASTER_ID)) instanceof PlayerEntity player) {
            this.caster = player;
        }

        return this.caster;
    }

    @Override
    protected void initDataTracker() {
        this.dataTracker.startTracking(CASTER_ID, 0);
        this.dataTracker.startTracking(TIER, 0);
        this.dataTracker.startTracking(SEED, getWorld().getRandom().nextLong());
        this.dataTracker.startTracking(HIT_TARGET, false);
    }

    public long getSeed() {
        return dataTracker.get(SEED);
    }

    public int getTier() {
        return dataTracker.get(TIER);
    }

    public void setTier(int tier) {
        dataTracker.set(TIER, tier);
    }

    public void setOnBlockHitEffects(List<Consumer<BlockHitResult>> effects) {
        this.onBlockHitEffects = effects;
    }

    public void setOnHitEffects(List<Consumer<Entity>> effects) {
        this.onHitEffects = effects;
    }

    public void setHitTarget(boolean hit) {
        this.dataTracker.set(HIT_TARGET, hit);
    }

    public boolean hasHitTarget() {
        return this.dataTracker.get(HIT_TARGET);
    }

    public float getOpacity(float tickDelta) {
        return this.hasHitTarget() ? (this.despawnTimer - tickDelta) / 10F : 1F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.age >= 60 && !this.getWorld().isClient) {
            discard();
            return;
        }

        if (this.hasHitTarget()) {
            this.setVelocity(Vec3d.ZERO);

            this.despawnTimer--;

            if (this.despawnTimer <= 0 && !this.getWorld().isClient) {
                this.discard();
            }
        } else {
            // Maintain constant velocity in the initial direction
            if (this.direction != null) {
                this.setVelocity(this.direction);
            }
        }
    }

    @Override
    public void setVelocity(double x, double y, double z) {
        super.setVelocity(x, y, z);
        this.direction = new Vec3d(x, y, z); // Store the direction whenever velocity is set
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        if (this.hasHitTarget()) {
            return;
        }

        super.onBlockHit(blockHitResult);
        this.setHitTarget(true);

        // Apply block hit effects
        for (Consumer<BlockHitResult> effect : onBlockHitEffects) {
            effect.accept(blockHitResult);
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        if (this.hasHitTarget() || entityHitResult.getEntity() == this.getCaster()) {
            return;
        }

        this.setHitTarget(true);
        this.onHitEffects.forEach(effect -> effect.accept(entityHitResult.getEntity()));
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putInt("CasterId", this.dataTracker.get(CASTER_ID));
        nbt.putInt("Tier", this.dataTracker.get(TIER));
        nbt.putLong("Seed", this.dataTracker.get(SEED));
        nbt.putBoolean("HitTarget", this.dataTracker.get(HIT_TARGET));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.dataTracker.set(CASTER_ID, nbt.getInt("CasterId"));
        this.dataTracker.set(TIER, nbt.getInt("Tier"));
        this.dataTracker.set(SEED, nbt.getLong("Seed"));
        this.dataTracker.set(HIT_TARGET, nbt.getBoolean("HitTarget"));
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }

    @Override
    protected float getGravity() {
        return 0.0f; // Ensure no gravity is applied
    }
}