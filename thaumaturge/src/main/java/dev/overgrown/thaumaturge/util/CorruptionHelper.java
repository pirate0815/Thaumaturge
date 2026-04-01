package dev.overgrown.thaumaturge.util;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.aether.AetherChunkData;
import dev.overgrown.aspectslib.aether.AetherManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

public class CorruptionHelper {

    private static final float SCALE = 0.5f;
    public static final Identifier VITIUM_ID = AspectsLib.identifier("vitium");


    public static void addCorruption(ServerWorld world, BlockPos pos, int amount) {
        AetherChunkData aetherChunkData = AetherManager.getAetherData(world, new ChunkPos(pos));
        aetherChunkData.modifyAspect(VITIUM_ID, (int) (amount * SCALE));
    }
}