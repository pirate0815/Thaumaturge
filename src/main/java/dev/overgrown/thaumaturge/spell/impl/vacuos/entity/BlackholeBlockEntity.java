package dev.overgrown.thaumaturge.spell.impl.vacuos.entity;

import dev.overgrown.thaumaturge.block.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlackholeBlockEntity extends BlockEntity {
    private static final double PULL_RADIUS = 10.0;
    private static final double DAMAGE_RADIUS = 2.5;
    private static final double PULL_STRENGTH = 0.45;
    private static final double MIN_PULL_STRENGTH = 0.05;
    private int age = 0;
    private static final int MAX_AGE = 20 * 10; // 10 seconds

    public BlackholeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BLACKHOLE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BlackholeBlockEntity blockEntity) {
        if (world.isClient) return;

        blockEntity.age++;
        if (blockEntity.age >= MAX_AGE) {
            world.removeBlock(pos, false);
            return;
        }

        Box box = Box.from(Vec3d.of(pos)).expand(PULL_RADIUS);
        world.getEntitiesByClass(Entity.class, box, e -> true).forEach(entity -> {
            Vec3d center = Vec3d.ofCenter(pos);
            Vec3d entityPos = entity.getPos();
            Vec3d direction = center.subtract(entityPos).normalize();

            // Calculate pull strength based on distance with improved falloff
            double distance = entityPos.distanceTo(center);

            // Use inverse square law for more realistic physics, but cap it to prevent excessive force
            double distanceFactor = Math.max(0.1, Math.min(1.0, (PULL_RADIUS - distance) / PULL_RADIUS));
            double currentPull = (PULL_STRENGTH * distanceFactor * distanceFactor) + MIN_PULL_STRENGTH;

            // Apply moderate increase in pull when closer to simulate event horizon
            if (distance < PULL_RADIUS * 0.4) {
                currentPull *= 1.25; // 25% stronger pull in inner region (reduced from 50%)
            }

            entity.addVelocity(
                    direction.x * currentPull,
                    direction.y * currentPull,
                    direction.z * currentPull
            );
            entity.velocityModified = true;

            // Damage entities within damage radius
            if (distance < DAMAGE_RADIUS) {
                // Scale damage based on how close to center (more damage closer to center)
                float damageAmount = (float) (4.0 * (1.0 - (distance / DAMAGE_RADIUS)));
                entity.damage((ServerWorld) world, world.getDamageSources().outOfWorld(), Math.max(1.5f, damageAmount));
            }
        });
    }
}