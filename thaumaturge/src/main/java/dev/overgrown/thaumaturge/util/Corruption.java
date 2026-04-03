package dev.overgrown.thaumaturge.util;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.aether.AetherChunkData;
import dev.overgrown.aspectslib.aether.AetherManager;
import dev.overgrown.thaumaturge.data_generator.ModBlockTags;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.random.Random;

import java.util.Optional;

public class Corruption {

    private static final float SCALE = 0.5f;
    public static final Identifier VITIUM_ID = AspectsLib.identifier("vitium");

    private static final int VITIUM_START_RUBIO_SPREAD = 100;
    private static final int VITIUM_PER_BLOCK = 20;


    public static void addCorruption(ServerWorld world, BlockPos pos, int amount) {
        AetherChunkData aetherChunkData = AetherManager.getAetherData(world, new ChunkPos(pos));
        int vititumLevel = aetherChunkData.getCurrentAether(VITIUM_ID) + (int) (amount * SCALE);

        Random random = world.getRandom();
        while (vititumLevel > VITIUM_START_RUBIO_SPREAD) {
            vititumLevel = vititumLevel - 1;

            BlockPos target = new BlockPos(
                    pos.getX() + random.nextBetweenExclusive(-3, 3),
                    pos.getY() + random.nextBetweenExclusive(-3, 3),
                    pos.getZ() + random.nextBetweenExclusive(-3, 3));

            if (world.isChunkLoaded(new ChunkPos(target).toLong()) && (world.getBottomY() <= pos.getY() && pos.getY() <= world.getTopY())) {
                BlockState currentState = world.getBlockState(target);
                Optional<BlockState> futureState = getCorruptedVersion(currentState);
                if (futureState.isPresent()) {
                    world.setBlockState(target, futureState.get());
                    vititumLevel = vititumLevel - VITIUM_PER_BLOCK;
                }
            }

        }
        aetherChunkData.setAspectLevel(VITIUM_ID, vititumLevel);
    }

    public static Optional<BlockState> getCorruptedVersion(BlockState state) {
        if (state.isIn(ModBlockTags.RUBICO_ROCK_REPLACABLE)) return Optional.ofNullable(ModBlocks.RUBICO_ROCK.getDefaultState());
        if (state.isIn(ModBlockTags.RUBICO_SAND_REPLACABLE)) return Optional.ofNullable(ModBlocks.RUBICO_SAND.getDefaultState());

        return Optional.empty();
    }
}