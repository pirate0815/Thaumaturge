package dev.overgrown.thaumaturge.spell.impl.ignis;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.utils.SpellContext;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class IgnisEffect implements AspectEffect {
    @Override
    public void apply(SpellContext context) {
        int baseTicks = 100;
        int duration = (int) (baseTicks * context.getAmplifier());
        World world = context.getWorld();

        // Entity ignition
        if (context.getTarget() != null) {
            Entity target = context.getTarget();
            target.setFireTicks(duration);
            return; // Exit early to skip block ignition
        }

        // Block ignition
        BlockPos pos = context.getPos();
        if (pos != null) {
            if (world.getBlockState(pos).isAir()) {
                world.setBlockState(pos, Blocks.FIRE.getDefaultState());
            }

            int radius = 1 + context.getAmplifier();
            for (int i = 0; i < radius * 2; i++) {
                BlockPos newPos = pos.add(
                        world.random.nextInt(radius * 2) - radius,
                        world.random.nextInt(3) - 1,
                        world.random.nextInt(radius * 2) - radius
                );

                if (world.getBlockState(newPos).isAir() &&
                        world.getBlockState(newPos.down()).isSolidBlock(world, newPos.down())) {
                    world.setBlockState(newPos, Blocks.FIRE.getDefaultState());
                }
            }
        }
    }
}