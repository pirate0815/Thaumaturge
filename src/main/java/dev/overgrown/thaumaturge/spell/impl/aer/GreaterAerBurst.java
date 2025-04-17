package dev.overgrown.thaumaturge.spell.impl.aer;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.BlockPos;

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
    }
}