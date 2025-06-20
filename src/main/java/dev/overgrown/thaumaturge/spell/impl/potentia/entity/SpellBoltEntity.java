package dev.overgrown.thaumaturge.spell.impl.potentia.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
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

    public SpellBoltEntity(EntityType<? extends ThrownEntity> type, World world) {
        super(type, world);
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
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(CASTER_ID, 0);
        builder.add(TIER, 0);
        builder.add(SEED, getWorld().getRandom().nextLong());
        builder.add(HIT_TARGET, false);
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
        }
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
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false; // Not damageable
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putInt("CasterId", this.dataTracker.get(CASTER_ID));
        view.putInt("Tier", this.dataTracker.get(TIER));
        view.putLong("Seed", this.dataTracker.get(SEED));
        view.putBoolean("HitTarget", this.dataTracker.get(HIT_TARGET));
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.dataTracker.set(CASTER_ID, view.getInt("CasterId", 0));
        this.dataTracker.set(TIER, view.getInt("Tier", 0));
        this.dataTracker.set(SEED, view.getLong("Seed", this.getWorld().getRandom().nextLong()));
        this.dataTracker.set(HIT_TARGET, view.getBoolean("HitTarget", false));
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }
}