package dev.overgrown.thaumaturge.spell.impl.perditio;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PerditioEffect implements AspectEffect {
    // Base hardness threshold without power modifier
    private static final float BASE_HARDNESS_THRESHOLD = 2.0f;

    // Base radii for different tiers
    private static final int BASE_RADIUS_LESSER = 0;   // Single block
    private static final int BASE_RADIUS_ADVANCED = 0; // Single block
    private static final int BASE_RADIUS_GREATER = 1;  // 3x3x3 cube

    // Scatter radii for different tiers
    private static final int SCATTER_RADIUS_LESSER = 1;   // 3x3 area (2 radius = 5 blocks, too big) -> 1 radius = 3x3
    private static final int SCATTER_RADIUS_ADVANCED = 1; // 3x3x3 cube
    private static final int SCATTER_RADIUS_GREATER = 3;  // 7x7x7 cube

    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.addEffect(caster -> {
            if (!(caster.getWorld() instanceof ServerWorld world)) return;

            BlockPos center = caster.getBlockPos().down();
            boolean hasPower = delivery.getPowerMultiplier() > 1.0f;
            boolean hasScatter = delivery.getScatterSize() > 0;

            // Calculate final radius based on tier and scatter
            int radius = hasScatter ? SCATTER_RADIUS_LESSER : BASE_RADIUS_LESSER;

            // Break a square area at feet level
            breakSquare(world, center, radius, hasPower);
        });
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addBlockHitEffect(hit -> {
            if (!(delivery.getCaster().getWorld() instanceof ServerWorld world)) return;

            BlockPos center = hit.getBlockPos();
            boolean hasPower = delivery.getPowerMultiplier() > 1.0f;
            boolean hasScatter = delivery.getScatterSize() > 0;

            // Calculate final radius based on tier and scatter
            int radius = hasScatter ? SCATTER_RADIUS_ADVANCED : BASE_RADIUS_ADVANCED;

            // Break a cube centered on the target block
            breakCube(world, center, radius, hasPower);
        });
    }

    @Override
    public void apply(AoeSpellDelivery delivery) {
        delivery.addEffect(center -> {
            if (!(delivery.getCasterWorld() instanceof ServerWorld world)) return;

            boolean hasPower = delivery.getPowerMultiplier() > 1.0f;
            boolean hasScatter = delivery.getScatterSize() > 0;

            // Calculate final radius based on tier and scatter
            int radius = hasScatter ? SCATTER_RADIUS_GREATER : BASE_RADIUS_GREATER;

            // Break a cube centered on the caster
            breakCube(world, center, radius, hasPower);
        });
    }

    // Break a square area at a specific Y level
    private void breakSquare(ServerWorld world, BlockPos center, int radius, boolean canBreakHard) {
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;
        int y = center.getY();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                breakBlockIfPossible(world, new BlockPos(x, y, z), canBreakHard);
            }
        }
    }

    // Break a cube centered on a position
    private void breakCube(ServerWorld world, BlockPos center, int radius, boolean canBreakHard) {
        int minX = center.getX() - radius;
        int maxX = center.getX() + radius;
        int minY = center.getY() - radius;
        int maxY = center.getY() + radius;
        int minZ = center.getZ() - radius;
        int maxZ = center.getZ() + radius;

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    breakBlockIfPossible(world, new BlockPos(x, y, z), canBreakHard);
                }
            }
        }
    }

    private void breakBlockIfPossible(ServerWorld world, BlockPos pos, boolean canBreakHard) {
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();

        // Skip air and unbreakable blocks
        if (state.isAir() || state.getHardness(world, pos) < 0) return;

        // Check hardness threshold if we don't have power modifier
        if (!canBreakHard && state.getHardness(world, pos) > BASE_HARDNESS_THRESHOLD) return;

        // Break the block and drop items
        world.breakBlock(pos, true);
    }
}