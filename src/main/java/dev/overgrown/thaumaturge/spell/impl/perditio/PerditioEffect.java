package dev.overgrown.thaumaturge.spell.impl.perditio;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ScatterModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class PerditioEffect implements AspectEffect {

    private static final float BASE_POWER = 5.0f; // Can break blocks with hardness <= 5.0
    private static final float POWER_MULTIPLIER = 2.0f; // Doubles the breaking power when power modifier is present
    private static final float SCATTER_POWER_REDUCTION = 0.7f; // 30% reduction when scatter is present

    @Override
    public void applySelf(SelfSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        ServerWorld world = (ServerWorld) caster.getWorld();
        BlockPos casterPos = caster.getBlockPos();

        boolean hasPower = hasPowerModifier(delivery.getModifiers());
        boolean hasScatter = hasScatterModifier(delivery.getModifiers());

        float effectivePower = calculateEffectivePower(hasPower, hasScatter);
        int range = hasScatter ? 2 : 1;

        // Break blocks vertically below the caster
        for (int i = 1; i <= range; i++) {
            BlockPos targetPos = casterPos.down(i);
            breakBlock(world, targetPos, effectivePower, caster);
        }
    }

    @Override
    public void applyTargeted(TargetedSpellDelivery delivery) {
        if (!delivery.isBlockTarget()) return;

        ServerPlayerEntity caster = delivery.getCaster();
        ServerWorld world = (ServerWorld) caster.getWorld();
        BlockPos targetPos = delivery.getBlockPos();

        // Add null check to prevent potential NPE
        if (targetPos == null) return;

        boolean hasPower = hasPowerModifier(delivery.getModifiers());
        boolean hasScatter = hasScatterModifier(delivery.getModifiers());

        float effectivePower = calculateEffectivePower(hasPower, hasScatter);
        int range = hasScatter ? 1 : 0; // 1 means 2x2 area, 0 means single block

        // Break blocks in the specified area
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos currentPos = targetPos.add(x, y, z);
                    breakBlock(world, currentPos, effectivePower, caster);
                }
            }
        }
    }

    @Override
    public void applyAoe(AoeSpellDelivery delivery) {
        ServerPlayerEntity caster = delivery.getCaster();
        ServerWorld world = (ServerWorld) caster.getWorld();
        BlockPos center = delivery.getCenter();

        boolean hasPower = hasPowerModifier(delivery.getModifiers());
        boolean hasScatter = hasScatterModifier(delivery.getModifiers());

        float effectivePower = calculateEffectivePower(hasPower, hasScatter);
        double radius = delivery.getRadius() * (hasScatter ? 2.0f : 1.0f);

        // Calculate the area to break
        int minX = (int) Math.floor(center.getX() - radius);
        int minY = (int) Math.floor(center.getY() - radius);
        int minZ = (int) Math.floor(center.getZ() - radius);
        int maxX = (int) Math.ceil(center.getX() + radius);
        int maxY = (int) Math.ceil(center.getY() + radius);
        int maxZ = (int) Math.ceil(center.getZ() + radius);

        // Iterate through all blocks in the area
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos currentPos = new BlockPos(x, y, z);
                    if (center.getSquaredDistance(currentPos.getX(), currentPos.getY(), currentPos.getZ()) <= radius * radius) {
                        breakBlock(world, currentPos, effectivePower, caster);
                    }
                }
            }
        }
    }

    private void breakBlock(ServerWorld world, BlockPos pos, float effectivePower, ServerPlayerEntity caster) {
        BlockState state = world.getBlockState(pos);
        FluidState fluidState = state.getFluidState();

        // Skip if it's bedrock, fluid, or air
        if (state.getBlock() == Blocks.BEDROCK ||
                !fluidState.isEmpty() ||
                state.isAir()) {
            return;
        }

        // Check if the block is breakable with current power
        float hardness = state.getHardness(world, pos);
        if (hardness <= effectivePower && hardness >= 0) {
            world.breakBlock(pos, true, caster);
        }
    }

    private boolean hasPowerModifier(List<ModifierEffect> modifiers) {
        return modifiers.stream().anyMatch(mod -> mod instanceof PowerModifierEffect);
    }

    private boolean hasScatterModifier(List<ModifierEffect> modifiers) {
        return modifiers.stream().anyMatch(mod -> mod instanceof ScatterModifierEffect);
    }

    private float calculateEffectivePower(boolean hasPower, boolean hasScatter) {
        float power = BASE_POWER;
        if (hasPower) {
            power *= POWER_MULTIPLIER;
        }
        if (hasScatter) {
            power *= SCATTER_POWER_REDUCTION;
        }
        return power;
    }
}