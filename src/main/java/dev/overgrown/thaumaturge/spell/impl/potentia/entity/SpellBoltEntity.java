package dev.overgrown.thaumaturge.spell.impl.potentia.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class SpellBoltEntity extends Entity {
    private UUID casterUuid;
    private List<Consumer<Entity>> onHitEffects = new ArrayList<>();
    private static final TrackedData<Integer> TIER = DataTracker.registerData(SpellBoltEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Long> SEED = DataTracker.registerData(SpellBoltEntity.class, TrackedDataHandlerRegistry.LONG);
    private int life;

    public SpellBoltEntity(EntityType<?> type, World world) {
        super(type, world);
        this.life = 2;
    }

    public void setCaster(PlayerEntity caster) {
        this.casterUuid = caster.getUuid();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(TIER, 0);
        builder.add(SEED, getWorld().getRandom().nextLong());
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
        // Read custom data from NBT (if needed)
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
        // Write custom data to NBT (if needed)
    }

    public long getSeed() { return dataTracker.get(SEED); }
    public int getTier() { return dataTracker.get(TIER); }

    public void setTier(int tier) { dataTracker.set(TIER, tier); }

    public void setOnHitEffects(List<Consumer<Entity>> effects) {
        this.onHitEffects = effects;
    }

    @Override
    public void tick() {
        super.tick();
        if (life-- <= 0) {
            discard();
            return;
        }

        Vec3d startPos = getPos();
        Vec3d endPos = startPos.add(getVelocity());
        EntityHitResult hitResult = ProjectileUtil.raycast(
                this,
                startPos,
                endPos,
                getBoundingBox().stretch(getVelocity()),
                entity -> {
                    if (entity.isSpectator() || !entity.isAlive()) {
                        return false;
                    }
                    // Exclude the caster from collision
                    return !entity.getUuid().equals(casterUuid);
                },
                getVelocity().lengthSquared()
        );

        if (hitResult != null) {
            Entity target = hitResult.getEntity();
            onHitEffects.forEach(effect -> effect.accept(target));
            discard();
        } else {
            setVelocity(getVelocity().multiply(0.95));
            move(MovementType.SELF, getVelocity());
            ProjectileUtil.setRotationFromVelocity(this, 1.0f);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false; // Not damageable
    }
}