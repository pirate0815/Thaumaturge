/**
 * AdvancedAerLaunch.java
 * <p>
 * The Advanced Aer Launch spell:
 * 1. Performs a ray-cast from the player's eye position in the direction they're looking
 * 2. If it hits a living entity, launches the target upward
 * 3. Creates visual effects at the target's location
 * 4. Plays a sound effect when successfully cast
 * <p>
 * This spell requires the Advanced Aer Foci to be equipped in a gauntlet.
 *
 * @see dev.overgrown.thaumaturge.spell.registry.SpellEntry.SpellExecutor
 * @see dev.overgrown.thaumaturge.item.ModItems#ADVANCED_AER_FOCI
 * @see dev.overgrown.thaumaturge.spell.SpellHandler#getRequiredFociId
 */
package dev.overgrown.thaumaturge.spell.impl.aer;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class AdvancedAerLaunch implements SpellEntry.SpellExecutor {
    /**
     * Unique identifier for this spell, used in the SpellRegistry
     * @see dev.overgrown.thaumaturge.spell.SpellRegistry#registerSpell
     */
    public static final Identifier ID = Thaumaturge.identifier("advanced_aer_launch");

    /**
     * Executes the Advanced Aer Launch spell when cast by a player
     *
     * @param player The player who cast the spell
     */
    @Override
    public void execute(ServerPlayerEntity player) {
        // Define maximum range for the ray-cast
        double maxDistance = 64.0;

        // Calculate start and end positions for the ray-cast
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(player.getRotationVector().multiply(maxDistance));

        // Perform ray-cast to find entities in the line of sight
        EntityHitResult hit = ProjectileUtil.raycast(
                player, start, end, new Box(start, end),
                entity -> !entity.isSpectator() && entity.isAlive() && entity != player, maxDistance);

        // If an entity was hit and it's a living entity
        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            // Apply upward velocity to the target
            target.addVelocity(0, 0.9, 0);
            target.velocityModified = true;

            // Play spell sound effect
            player.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);

            // Spawn wind charge particles at target's feet for visual effect
            ServerWorld world = (ServerWorld) player.getWorld();
            world.spawnParticles(ParticleTypes.GUST_EMITTER_SMALL,
                    target.getX(), target.getY(), target.getZ(), 1, 0.5, 0.2, 0.5, 0.1);
        }
        // Note: No effects or feedback if no target was hit
    }
}