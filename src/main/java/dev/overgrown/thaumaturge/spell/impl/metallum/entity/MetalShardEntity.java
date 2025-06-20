package dev.overgrown.thaumaturge.spell.impl.metallum.entity;

import dev.overgrown.thaumaturge.entity.ModEntities;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MetalShardEntity extends PersistentProjectileEntity {
    private static final TrackedData<Integer> LIFETIME = DataTracker.registerData(MetalShardEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> AGE = DataTracker.registerData(MetalShardEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> PUNCH = DataTracker.registerData(MetalShardEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public MetalShardEntity(EntityType<MetalShardEntity> entityType, World world) {
        super(entityType, world);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    public MetalShardEntity(World world, LivingEntity owner) {
        super(ModEntities.METAL_SHARD, owner, world, new ItemStack(Items.IRON_NUGGET), null);
        this.pickupType = PickupPermission.DISALLOWED;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(LIFETIME, 100);
        builder.add(AGE, 0);
        builder.add(PUNCH, 0); // Initialize punch value
    }

    public void setLifetime(int ticks) {
        this.dataTracker.set(LIFETIME, ticks);
    }

    public void setPunch(int punch) {
        this.dataTracker.set(PUNCH, punch);
    }

    @Override
    protected void knockback(LivingEntity target, DamageSource source) {
        double d = this.dataTracker.get(PUNCH);
        if (d > 0.0) {
            double e = Math.max(0.0, 1.0 - target.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE));
            Vec3d vec3d = this.getVelocity().multiply(1.0, 0.0, 1.0).normalize().multiply(d * 0.6 * e);
            if (vec3d.lengthSquared() > 0.0) {
                target.addVelocity(vec3d.x, 0.1, vec3d.z);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        int age = this.dataTracker.get(AGE);
        int lifetime = this.dataTracker.get(LIFETIME);

        if (age >= lifetime) {
            this.discard();
            return;
        }

        this.dataTracker.set(AGE, age + 1);

        if (this.getWorld() instanceof ServerWorld serverWorld) {
            float progress = (float) age / lifetime;
            int particleCount = progress > 0.8f ? 3 : 1;

            for (int i = 0; i < particleCount; i++) {
                serverWorld.spawnParticles(ParticleTypes.SMOKE,
                        this.getX(), this.getY(), this.getZ(),
                        1, 0, 0, 0, 0.01);
            }
        }
    }

    @Override
    protected ItemStack getDefaultItemStack() {
        return new ItemStack(Items.IRON_NUGGET);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("Lifetime", this.dataTracker.get(LIFETIME));
        view.putInt("Age", this.dataTracker.get(AGE));
        view.putInt("Punch", this.dataTracker.get(PUNCH));
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.dataTracker.set(LIFETIME, view.getInt("Lifetime", 100));
        this.dataTracker.set(AGE, view.getInt("Age", 0));
        this.dataTracker.set(PUNCH, view.getInt("Punch", 0));
    }
}