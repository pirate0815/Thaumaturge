package dev.overgrown.thaumaturge.spell.impl.aer;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.List;

public class GreaterAerBurst implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("greater_aer_burst");

    @Override
    public void execute(ServerPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();
        Box aoeBox = new Box(playerPos).expand(5);
        List<Entity> entities = player.getWorld().getOtherEntities(
                player, aoeBox, entity -> entity instanceof LivingEntity && entity.isAlive() &&
                        player.squaredDistanceTo(entity) <= 25);

        for (Entity entity : entities) {
            entity.addVelocity(0, 1.1, 0);
            entity.velocityModified = true;
        }

        if (player.getRandom().nextFloat() < 0.15f) {
            player.addVelocity(0, 1.1, 0);
            player.velocityModified = true;
        }

        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.5f, 0.8f);

        // Spawn expanding white dust particles
        DustParticleEffect dustEffect = new DustParticleEffect(0xFFFFFF, 1.0F);
        int particles = 50; // Increased particle count for better visibility
        double radius = 1.0; // Initial radius
        double expansionSpeed = 0.5; // Expansion speed multiplier

        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d center = player.getPos().add(0, 1.0, 0); // Center above player

        for (int i = 0; i < particles; i++) {
            double angle = (2 * Math.PI * i) / particles;
            // Calculate direction with initial radius
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;

            // Calculate velocity for outward expansion
            double velocityX = Math.cos(angle) * expansionSpeed;
            double velocityZ = Math.sin(angle) * expansionSpeed;

            world.spawnParticles(
                    dustEffect,
                    center.x + dx, center.y, center.z + dz, // Start at edge of circle
                    1,
                    velocityX, 0.1, velocityZ, // Add slight upward velocity
                    0.5 // Speed multiplier
            );
        }
    }
}