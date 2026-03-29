package dev.overgrown.thaumaturge.util;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.corruption.CorruptionAPI;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

public class CorruptionHelper {

    private static final float SCALE = 0.5f;


    public static void addCorruption(ServerWorld world, BlockPos pos, int amount) {

        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        Optional<RegistryKey<Biome>> optionalKey = biomeEntry.getKey();
        if (optionalKey.isEmpty()) {
            return;
        }

        CorruptionAPI.forceCorruption(world, new ChunkPos(pos), (int) (amount * SCALE));
    }
}