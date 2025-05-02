package dev.overgrown.thaumaturge.spell.combination;

import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class GustboundDash implements SpellEntry.SpellExecutor {
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