package dev.overgrown.thaumaturge.spell.impl.alienis;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.utils.ModSounds;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import java.util.EnumSet;
import java.util.Set;

public class AlienisEffect implements AspectEffect {

    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.addEffect(entity -> {
            if (!(entity instanceof ServerPlayerEntity player)) return;

            MinecraftServer server = player.getServer();
            assert server != null;
            ServerWorld targetWorld = server.getOverworld();

            // Get player's spawn position in the target world or use world spawn
            ServerPlayerEntity.Respawn respawn = player.getRespawn();
            BlockPos spawnPos = (respawn != null && respawn.dimension() == targetWorld.getRegistryKey())
                    ? respawn.pos()
                    : targetWorld.getSpawnPos();

            Vec3d spawnPoint = Vec3d.ofBottomCenter(spawnPos);

            // Teleport with correct parameters
            if (player.teleport(
                    targetWorld,
                    spawnPoint.x,
                    spawnPoint.y,
                    spawnPoint.z,
                    EnumSet.noneOf(PositionFlag.class),
                    player.getYaw(),
                    player.getPitch(),
                    true)) {
                playTeleportEffects(targetWorld, spawnPoint);
                player.fallDistance = 0;
            }
        });
    }

    private void playTeleportEffects(ServerWorld world, Vec3d pos) {
        world.playSound(null, pos.x, pos.y, pos.z,
                ModSounds.ALIENIS_SPELL_CAST, SoundCategory.PLAYERS, 1.0F, 1.0F);

        world.spawnParticles(ParticleTypes.PORTAL,
                pos.x, pos.y + 1, pos.z, 20,
                0.5, 0.5, 0.5, 0.1);
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addOnHitEffect(entity -> teleportEntityRandomly(entity, 16.0F));
    }

    @Override
    public void apply(AoeSpellDelivery delivery) {
        delivery.addEffect(pos -> {
            ServerWorld world = delivery.getCasterWorld();
            Box area = new Box(pos).expand(5.0);
            world.getEntitiesByClass(Entity.class, area, e -> true).forEach(entity -> teleportEntityRandomly(entity, 8.0F));
        });
    }

    private void teleportEntityRandomly(Entity entity, float diameter) {
        ServerWorld world = (ServerWorld) entity.getWorld();
        for (int i = 0; i < 32; ++i) {
            double x = entity.getX() + (world.random.nextDouble() - 0.5) * diameter;
            double z = entity.getZ() + (world.random.nextDouble() - 0.5) * diameter;

            // Get the top Y coordinate for the generated X and Z
            int topY = world.getTopY(Heightmap.Type.MOTION_BLOCKING, (int) x, (int) z);
            BlockPos topPos = BlockPos.ofFloored(x, topY, z);

            // Check if the block has collision and the block above is replaceable
            if (!world.getBlockState(topPos).getCollisionShape(world, topPos).isEmpty()
                    && world.getBlockState(topPos.up()).isReplaceable()) {

                Vec3d teleportVec = Vec3d.ofBottomCenter(topPos.up());
                Set<PositionFlag> flags = EnumSet.noneOf(PositionFlag.class);
                if (entity.teleport(world, teleportVec.x, teleportVec.y, teleportVec.z, flags, entity.getYaw(), entity.getPitch(), true)) {
                    world.playSound(null, teleportVec.x, teleportVec.y, teleportVec.z,
                            ModSounds.ALIENIS_SPELL_CAST, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    world.spawnParticles(ParticleTypes.PORTAL, teleportVec.x, teleportVec.y + 1, teleportVec.z, 10, 0.5, 0.5, 0.5, 0.05);
                    break;
                }
            }
        }
        entity.fallDistance = 0;
    }
}