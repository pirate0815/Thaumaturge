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
    public static final Identifier ID = Thaumaturge.identifier("advanced_aer_launch");

    @Override
    public void execute(ServerPlayerEntity player) {
        double maxDistance = 64.0;
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(player.getRotationVector().multiply(maxDistance));
        EntityHitResult hit = ProjectileUtil.raycast(
                player, start, end, new Box(start, end),
                entity -> !entity.isSpectator() && entity.isAlive() && entity != player, maxDistance);

        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            target.addVelocity(0, 0.9, 0);
            target.velocityModified = true;

            // Play sound
            player.getWorld().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ENTITY_BREEZE_SHOOT, SoundCategory.PLAYERS, 1.0f, 1.0f);

            // Spawn wind charge particles at target's feet
            ServerWorld world = (ServerWorld) player.getWorld();
            world.spawnParticles(ParticleTypes.GUST_EMITTER_SMALL,
                    target.getX(), target.getY(), target.getZ(), 1, 0.5, 0.2, 0.5, 0.1);
        }
    }
}