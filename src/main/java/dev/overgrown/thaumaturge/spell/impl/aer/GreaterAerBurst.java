/**
 * GreaterAerBurst.java
 * <p>
 * This class implements a higher-tier air spell that creates an area-of-effect upward burst.
 * The Greater Aer Burst spell:
 * 1. Affects entities within a 5-block radius of the player
 * 2. Launches affected entities upward with significant force
 * 3. Has a small chance (15%) to also affect the caster
 * 4. Creates visual effects with white dust particles in an expanding circle
 * 5. Plays a specialized sound effect at cast time
 * <p>
 * This spell requires the Greater Aer Foci to be equipped in a gauntlet.
 *
 * @see dev.overgrown.thaumaturge.spell.registry.SpellEntry.SpellExecutor
 * @see dev.overgrown.thaumaturge.item.ModItems#GREATER_AER_FOCI
 * @see dev.overgrown.thaumaturge.spell.SpellHandler#getRequiredFociId
 */
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

import java.util.List;

public class GreaterAerBurst implements SpellEntry.SpellExecutor {
    /**
     * Unique identifier for this spell, used in the SpellRegistry
     * @see dev.overgrown.thaumaturge.spell.SpellRegistry#registerSpell
     */
    public static final Identifier ID = Thaumaturge.identifier("greater_aer_burst");

    /**
     * Executes the Greater Aer Burst spell when cast by a player
     *
     * @param player The player who cast the spell
     */
    @Override
    public void execute(ServerPlayerEntity player) {
        // Calculate the area of effect centered on the player
        BlockPos playerPos = player.getBlockPos();
        Box aoeBox = new Box(playerPos).expand(5); // Define the area of effect - 5 block radius around the player

        // Find all entities within range (5 blocks), excluding the caster
        List<Entity> entities = player.getWorld().getOtherEntities(
                player, aoeBox, entity -> entity instanceof LivingEntity && entity.isAlive() &&
                        player.squaredDistanceTo(entity) <= 25);

        // Apply upward velocity to all affected entities
        for (Entity entity : entities) {
            entity.addVelocity(0, 1.1, 0);
            entity.velocityModified = true;
        }

        // 15% chance to also apply effect to the caster
        if (player.getRandom().nextFloat() < 0.15f) {
            player.addVelocity(0, 1.1, 0);
            player.velocityModified = true;
        }

        // Play spell sound effect with lower pitch for the "burst" feel
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.5f, 0.8f);

        // Create visual effect - expanding white dust particles in a circle
        DustParticleEffect dustEffect = new DustParticleEffect(0xFFFFFF, 1.0F);
        int particles = 50; // Higher particle count for better visibility
        double radius = 1.0; // Initial radius
        double expansionSpeed = 0.5; // Expansion speed multiplier

        ServerWorld world = (ServerWorld) player.getWorld();
        Vec3d center = player.getPos().add(0, 1.0, 0); // Center above player

        // Generate particles in a circle pattern that expands outward
        for (int i = 0; i < particles; i++) {
            double angle = (2 * Math.PI * i) / particles;
            // Calculate direction with initial radius
            double dx = Math.cos(angle) * radius;
            double dz = Math.sin(angle) * radius;

            // Calculate velocity for outward expansion
            double velocityX = Math.cos(angle) * expansionSpeed;
            double velocityZ = Math.sin(angle) * expansionSpeed;

            world.spawnParticles(dustEffect, center.x + dx, center.y, center.z + dz, 1, velocityX, 0.1, velocityZ, 0.5);
        }
    }
}