/**
 * AerMotusCombination.java
 * <p>
 * This class implements a combination spell that occurs when both Aer and Motus foci
 * are equipped simultaneously.
 * <p>
 * The Aer-Motus Combination spell:
 * 1. Propels the player forward in the direction they're looking
 * 2. Also includes an upward component to the movement
 * 3. Plays a sound effect when cast
 * <p>
 * This demonstrates the spell combination system where having multiple foci can
 * create new spell effects beyond the individual components.
 *
 * @see dev.overgrown.thaumaturge.spell.registry.SpellEntry.SpellExecutor
 * @see dev.overgrown.thaumaturge.spell.SpellRegistry#registerCombination
 * @see dev.overgrown.thaumaturge.item.ModItems#LESSER_AER_FOCI
 * @see dev.overgrown.thaumaturge.item.ModItems#LESSER_MOTUS_FOCI
 */
package dev.overgrown.thaumaturge.spell.combination;

import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class AerMotusCombination implements SpellEntry.SpellExecutor {
    /**
     * Executes the Aer-Motus combination spell when cast by a player
     *
     * @param player The player who cast the spell
     */
    @Override
    public void execute(ServerPlayerEntity player) {
        // Get the direction the player is looking and multiply by 1.5 for distance
        Vec3d lookVec = player.getRotationVector().multiply(1.5);

        // Apply velocity in the horizontal direction the player is facing plus upward
        player.addVelocity(lookVec.x, 0.5, lookVec.z);
        player.velocityModified = true;

        // Play spell sound effect with lower pitch for the combined spell
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.5f, 0.8f);
    }
}