package dev.overgrown.aspectslib.corruption;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.aether.AetherChunkData;
import dev.overgrown.aspectslib.aether.AetherManager;
import dev.overgrown.aspectslib.aether.AetherWorldState;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

import java.util.Collection;
import java.util.Set;

public class CorruptionAPI {

    /**
     * Checks if a chunk is pure (no Vitium)
     */
    public static boolean isChunkPure(ServerWorld world, ChunkPos chunkPos) {
        return CorruptionManager.isChunkPure(world, chunkPos);
    }

    /**
     * Checks if a chunk is tainted (has Vitium but not corrupted)
     */
    public static boolean isChunkTainted(ServerWorld world, ChunkPos chunkPos) {
        return CorruptionManager.isChunkTainted(world, chunkPos);
    }

    /**
     * Checks if a chunk is corrupted (Vitium dominates)
     */
    public static boolean isChunkCorrupted(ServerWorld world, ChunkPos chunkPos) {
        return CorruptionManager.isChunkCorrupted(world, chunkPos);
    }

    /**
     * Forces a chunk region to become corrupted by adding Vitium
     * @param world The server world
     * @param chunkPos The chunk position
     * @param vitiumAmount The amount of Vitium to add
     */
    public static void forceCorruption(ServerWorld world, ChunkPos chunkPos, int vitiumAmount) {
        AetherChunkData data = AetherManager.getAetherData(world, chunkPos);
        data.increaseVitiumMaximum(vitiumAmount);
        //data.increaseVitium(vitiumAmount);
    }

    /**
     * Purifies a chunk region by removing all Vitium
     * @param world The server wintorld
     * @param chunkPos The chunk position
     */
    public static void purifyChunk(ServerWorld world, ChunkPos chunkPos) {
        AetherManager.getAetherData(world, chunkPos).clearVitium();
    }

    /**
     * Gets the amount of Vitium in a chunk
     */
    public static int getVitiumAmount(ServerWorld world, ChunkPos chunkPos) {
        return AetherManager.getAetherData(world, chunkPos).getMaxAether(CorruptionManager.VITIUM_ID);
    }
}
