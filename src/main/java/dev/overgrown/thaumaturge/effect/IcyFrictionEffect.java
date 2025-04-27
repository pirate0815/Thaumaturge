package dev.overgrown.thaumaturge.effect;

import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class IcyFrictionEffect extends StatusEffect {
    public IcyFrictionEffect() {
        super(StatusEffectCategory.BENEFICIAL, 0x87CEEB); // Light blue color
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration % 5 == 0; // Apply every 5 ticks
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (!world.isClient()) {
            BlockPos pos = entity.getBlockPos();
            BlockPos ground = pos.down();
            if (world.getBlockState(ground).isSolidBlock(world, ground)
                    && world.getBlockState(pos).isAir()
                    && Blocks.SNOW.getDefaultState().canPlaceAt(world, pos)) {
                world.setBlockState(pos, Blocks.SNOW.getDefaultState());
            }
        }
        return true;
    }
}