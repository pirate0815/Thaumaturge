package dev.overgrown.thaumaturge.spell.impl.vacuos;

import dev.overgrown.thaumaturge.block.ModBlocks;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VacuosEffect implements AspectEffect {
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addBlockHitEffect(blockHit -> {
            World world = delivery.getWorld();
            BlockPos basePos = blockHit.getBlockPos().offset(blockHit.getSide());

            // Try placing at increasing heights
            BlockPos placementPos = null;
            for (int i = 3; i >= 1; i--) {
                BlockPos testPos = basePos.up(i);
                if (world.getBlockState(testPos).isReplaceable()) {
                    placementPos = testPos;
                    break;
                }
            }

            // Fallback to original position
            if (placementPos == null) {
                placementPos = basePos;
            }

            if (world.getBlockState(placementPos).isReplaceable()) {
                world.setBlockState(placementPos, ModBlocks.BLACKHOLE.getDefaultState());
            }
        });
    }
}