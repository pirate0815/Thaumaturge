package dev.overgrown.thaumaturge.spell.combination;

import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;

public class AerMotusCombination implements SpellEntry.SpellExecutor {
    @Override
    public void execute(ServerPlayerEntity player) {
        Vec3d lookVec = player.getRotationVector().multiply(1.5);
        player.addVelocity(lookVec.x, 0.5, lookVec.z);
        player.velocityModified = true;
        player.getWorld().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.5f, 0.8f);
    }
}