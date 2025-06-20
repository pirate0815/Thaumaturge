package dev.overgrown.thaumaturge.spell.impl.vinculum.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ArcaneMineEntity extends Entity {
    private int armTime = 40; // 2 seconds (40 ticks)
    private List<Consumer<Entity>> onHitEffects = new ArrayList<>();
    private List<Consumer<BlockHitResult>> onBlockHitEffects = new ArrayList<>();

    public ArcaneMineEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        // Initialize any data trackers if needed
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        // Make the mine invulnerable to damage
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getWorld().isClient) {
            if (armTime > 0) {
                armTime--;
                return;
            }

            // Check for entities colliding with the mine
            Box box = this.getBoundingBox().expand(0.2);
            List<Entity> entities = this.getWorld().getOtherEntities(this, box, e -> e instanceof LivingEntity);
            if (!entities.isEmpty()) {
                trigger(entities.getFirst());
                this.discard();
            }
        }
    }

    private void trigger(Entity entity) {
        // Apply entity effects
        onHitEffects.forEach(effect -> effect.accept(entity));

        // Apply block hit effects at the mine's position
        BlockPos pos = this.getBlockPos();
        BlockHitResult blockHit = new BlockHitResult(Vec3d.of(pos), Direction.UP, pos, false);
        onBlockHitEffects.forEach(effect -> effect.accept(blockHit));

        // Play explosion effect
        ((ServerWorld) this.getWorld()).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 1, 0, 0, 0, 0);
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    public void setArmTime(int ticks) {
        this.armTime = ticks;
    }

    public void setOnHitEffects(List<Consumer<Entity>> effects) {
        this.onHitEffects = new ArrayList<>(effects);
    }

    public void setOnBlockHitEffects(List<Consumer<BlockHitResult>> effects) {
        this.onBlockHitEffects = new ArrayList<>(effects);
    }

    @Override
    protected void readCustomData(ReadView view) {
        // Implement if needed
    }

    @Override
    protected void writeCustomData(WriteView view) {
        // Implement if needed
    }

    public Packet<ClientPlayPacketListener> getAddEntityPacket() {
        return new EntitySpawnS2CPacket(
                this.getId(),
                this.getUuid(),
                this.getX(),
                this.getY(),
                this.getZ(),
                this.getPitch(),
                this.getYaw(),
                this.getType(),
                0,
                this.getVelocity(),
                this.getHeadYaw()
        );
    }
}