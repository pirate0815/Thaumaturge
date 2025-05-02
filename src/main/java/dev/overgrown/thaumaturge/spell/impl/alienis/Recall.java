package dev.overgrown.thaumaturge.spell.impl.alienis;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

public class Recall implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("recall");

    @Override
    public void execute(ServerPlayerEntity caster) {
        MinecraftServer server = caster.getServer();
        if (server == null) return;

        // Always target the Overworld's spawn point
        ServerWorld targetWorld = server.getOverworld();

        // Get the actual spawn position with safe coordinates
        TeleportTarget spawnTarget = caster.getRespawnTarget(false, TeleportTarget.NO_OP);
        if (spawnTarget == null) {
            // Fallback to world spawn if no valid position found
            BlockPos spawnPos = targetWorld.getSpawnPos();
            spawnTarget = new TeleportTarget(
                    targetWorld,
                    Vec3d.ofCenter(spawnPos),
                    Vec3d.ZERO,
                    caster.getYaw(),
                    caster.getPitch(),
                    TeleportTarget.NO_OP
            );
        }

        // Teleport the player using the TeleportTarget
        caster.teleportTo(spawnTarget);

        // Reset fall distance and play effects
        caster.fallDistance = 0;
        targetWorld.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        targetWorld.spawnParticles(ParticleTypes.PORTAL,
                caster.getX(), caster.getY() + 1, caster.getZ(), 20,
                0.5, 0.5, 0.5, 0.1);
    }
}