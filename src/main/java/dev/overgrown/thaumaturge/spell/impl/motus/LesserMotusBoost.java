/**
 * LesserMotusBoost.java
 * <p>
 * Implementation of the Lesser Motus Boost spell.
 * This spell provides horizontal movement in the direction the player is facing.
 */
package dev.overgrown.thaumaturge.spell.impl.motus;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

public class LesserMotusBoost implements SpellEntry.SpellExecutor {
    /**
     * Unique identifier for this spell, used in the SpellRegistry
     * @see dev.overgrown.thaumaturge.spell.SpellRegistry#registerSpell
     */
    public static final Identifier ID = Thaumaturge.identifier("lesser_motus_boost");

    @Override
    public void execute(ServerPlayerEntity player) {
        // Get the direction the player is looking (horizontal only)
        Vec3d lookVec = player.getRotationVector();
        lookVec = new Vec3d(lookVec.x, 0, lookVec.z).normalize().multiply(1.5);

        // Apply velocity in the looking direction
        player.addVelocity(lookVec.x, lookVec.y, lookVec.z);
        player.velocityModified = true;

        // Play breeze shoot sound effect
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}