package dev.overgrown.thaumaturge.spell.impl.alienis;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerPosition;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.entity.Entity;

public class Recall implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("recall");

    @Override
    public void execute(ServerPlayerEntity caster) {
        ServerPlayerEntity.Respawn respawn = caster.getRespawn();
        RegistryKey<World> spawnDimension = (respawn != null) ? respawn.dimension() : World.OVERWORLD;
        BlockPos spawnPos = (respawn != null) ? respawn.pos() : null;

        MinecraftServer server = caster.getServer();
        if (server == null) {
            return;
        }
        ServerWorld targetWorld = server.getWorld(spawnDimension);

        // Fallback to overworld if spawn is invalid
        if (spawnPos == null || targetWorld == null) {
            targetWorld = server.getOverworld();
            spawnPos = targetWorld.getSpawnPos();
        }

        // Find safe spawn position
        Vec3d safeSpawn = Dismounting.findRespawnPos(EntityType.PLAYER, targetWorld, spawnPos, false);
        if (safeSpawn == null) {
            // Fallback to top of the world at spawn's X/Z
            BlockPos topPos = targetWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING, spawnPos);
            safeSpawn = new Vec3d(topPos.getX() + 0.5, topPos.getY() + 1.0, topPos.getZ() + 0.5);
        }

        // Teleport the player to the safe position
        caster.teleport(targetWorld,
                safeSpawn.getX(), safeSpawn.getY(), safeSpawn.getZ(),
                PositionFlag.VALUES, caster.getYaw(), caster.getPitch(), false);
        caster.fallDistance = 0;

        // Play effects
        targetWorld.playSound(null, caster.getX(), caster.getY(), caster.getZ(),
                SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F);
        targetWorld.spawnParticles(ParticleTypes.PORTAL,
                caster.getX(), caster.getY() + 1, caster.getZ(), 20,
                0.5, 0.5, 0.5, 0.1);
    }
}