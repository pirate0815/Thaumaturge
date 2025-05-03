package dev.overgrown.thaumaturge.spell.impl.permutatio;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.projectile.ProjectileUtil;

import java.util.EnumSet;

public class Exchange implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("exchange");

    @Override
    public void execute(ServerPlayerEntity player) {
        double maxDistance = 64.0;
        Vec3d start = player.getEyePos();
        Vec3d end = start.add(player.getRotationVector().multiply(maxDistance));

        EntityHitResult hit = ProjectileUtil.raycast(
                player, start, end, new Box(start, end),
                entity -> !entity.isSpectator() && entity.isAlive() && entity != player, maxDistance
        );

        if (hit != null && hit.getEntity() instanceof LivingEntity target) {
            // Save original positions and rotations
            Vec3d playerPos = player.getPos();
            float playerYaw = player.getYaw();
            float playerPitch = player.getPitch();

            Vec3d targetPos = target.getPos();
            float targetYaw = target.getYaw();
            float targetPitch = target.getPitch();

            // Teleport player to target's position
            ServerWorld world = (ServerWorld) player.getWorld();
            player.teleport(world, targetPos.x, targetPos.y, targetPos.z, EnumSet.noneOf(PositionFlag.class), targetYaw, targetPitch, false);

            // Teleport target to player's original position
            target.refreshPositionAndAngles(playerPos.x, playerPos.y, playerPos.z, playerYaw, playerPitch);
            target.setVelocity(Vec3d.ZERO);
            target.velocityModified = true;

            // Visual and sound effects
            world.spawnParticles(ParticleTypes.PORTAL, playerPos.x, playerPos.y + 1, playerPos.z,
                    20, 0.5, 0.5, 0.5, 0.1);
            world.spawnParticles(ParticleTypes.PORTAL, targetPos.x, targetPos.y + 1, targetPos.z,
                    20, 0.5, 0.5, 0.5, 0.1);

            world.playSound(null, playerPos.x, playerPos.y, playerPos.z,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
            world.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                    SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0f, 1.0f);
        }
    }
}