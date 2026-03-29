package dev.overgrown.aspectslib.corruption;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.aether.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CorruptionManager {
    public static final Identifier VITIUM_ID = AspectsLib.identifier("vitium");
    private static final Random RANDOM = new Random();

    private record AspectConsumptionResult(Identifier aspectId, int previousAmount,
                                           int newAmount, int vitiumBefore, int vitiumAfter) {
    }

    // Configuration
    private static final int CORRUPTION_CHECK_INTERVAL = 20; // 1 seconds
    private static final int CORRUPTION_PER_BLOCK = 20;

    public static void initialize() {
        ServerTickEvents.START_SERVER_TICK.register(CorruptionManager::onServerTick);
    }

    private static void onServerTick(MinecraftServer server) {
        long time = server.getOverworld().getTime();

        // Only check every CORRUPTION_CHECK_INTERVAL ticks for performance
        if (time % CORRUPTION_CHECK_INTERVAL != 0) {
            return;
        }

        for (ServerWorld world : server.getWorlds()) {
            processWorldCorruption(world, time);
        }
    }

    private static void processWorldCorruption(ServerWorld world, long currentTime) {
        // Get all loaded chunks
        Set<ChunkPos> loadedChunks = getLoadedChunks(world);

        // Process each chunk
        for (ChunkPos chunkPos : loadedChunks) {
            AetherChunkData aetherChunkDataData = AetherManager.getAetherData(world, chunkPos);
            int currentVitium = aetherChunkDataData.getCurrentAether(VITIUM_ID);
            int maxVitium = aetherChunkDataData.getMaxAether(VITIUM_ID);

            // Corrupt (current) Aspects to Vitium
            if (maxVitium > 0 && currentVitium < maxVitium) {
                AspectsLib.LOGGER.debug("Corrupting other Aspects to Vitium");
                Optional<Identifier> randomAspect = aetherChunkDataData.getAspectIds().stream()
                        .skip(ThreadLocalRandom.current().nextLong(aetherChunkDataData.getAspectIds().size())).findAny();
                if (randomAspect.isPresent()) {
                    Identifier aspect = randomAspect.get();
                    int amount = Math.min(aetherChunkDataData.getCurrentAether(aspect), 5);
                    if (amount > 0) {
                        aetherChunkDataData.harvestAether(aspect, amount);
                        aetherChunkDataData.increaseVitium(amount);
                    }
                }
            }

            if (currentVitium > CORRUPTION_PER_BLOCK && world.getRandom().nextInt(10) == 0) {
                net.minecraft.util.math.random.Random random = world.getRandom();
                BlockPos blockPos = chunkPos.getBlockPos(random.nextInt(16), random.nextBetweenExclusive(world.getBottomY(), world.getTopY()), random.nextInt(16));
                if (world.getBlockState(blockPos).isIn(BlockTags.SCULK_REPLACEABLE)) {
                    world.setBlockState(blockPos, Blocks.SCULK.getDefaultState());
                }

                // Play sculk spread sound effect
                world.playSound(
                        null, // player - null means all nearby players will hear it
                        blockPos.getX() + 0.5,
                        blockPos.getY() + 0.5,
                        blockPos.getZ() + 0.5,
                        net.minecraft.sound.SoundEvents.BLOCK_SCULK_SPREAD, // The sculk spread sound
                        net.minecraft.sound.SoundCategory.BLOCKS,
                        1.0f, // volume
                        0.8f + RANDOM.nextFloat() * 0.4f // pitch variation (0.8 to 1.2)
                );

                AspectsLib.LOGGER.debug("Placed sculk at {} in chunk {}", blockPos, chunkPos);
                aetherChunkDataData.harvestVitium(CORRUPTION_PER_BLOCK);
            }



        }
    }


    private static Set<ChunkPos> getLoadedChunks(ServerWorld world) {
        Set<ChunkPos> loadedChunks = new HashSet<>();

        // Collect chunks around players (within view distance)
        for (ServerPlayerEntity player : world.getPlayers()) {
            ChunkPos playerChunk = player.getChunkPos();
            int viewDistance = world.getServer().getPlayerManager().getViewDistance();

            // Add chunks in a radius around each player
            for (int x = -viewDistance; x <= viewDistance; x++) {
                for (int z = -viewDistance; z <= viewDistance; z++) {
                    ChunkPos chunkPos = new ChunkPos(playerChunk.x + x, playerChunk.z + z);
                    // Check if chunk is actually loaded
                    if (world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
                        loadedChunks.add(chunkPos);
                    }
                }
            }
        }

        return loadedChunks;
    }

    // Chunk-based API access
    public static boolean isChunkCorrupted(ServerWorld world, ChunkPos chunkPos) {
        AetherChunkData aetherChunkDataData = AetherManager.getAetherData(world, chunkPos);
        return aetherChunkDataData.isCorrupted();
    }

    public static boolean isChunkTainted(ServerWorld world, ChunkPos chunkPos) {
        AetherChunkData aetherChunkDataData = AetherManager.getAetherData(world, chunkPos);
        return (aetherChunkDataData.getMaxAether(VITIUM_ID) > 0 || aetherChunkDataData.getCurrentAether(VITIUM_ID) > 0)
                && !aetherChunkDataData.isCorrupted();
    }

    public static boolean isChunkPure(ServerWorld world, ChunkPos chunkPos) {
        AetherChunkData aetherChunkDataData = AetherManager.getAetherData(world, chunkPos);
        return aetherChunkDataData.getMaxAether(VITIUM_ID) == 0 && aetherChunkDataData.getCurrentAether(VITIUM_ID) == 0;
    }
}
