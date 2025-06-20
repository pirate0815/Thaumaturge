package dev.overgrown.thaumaturge.spell.impl.herba;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldEvents;

public class HerbaEffect implements AspectEffect {

    @Override
    public void apply(SelfSpellDelivery delivery) {
        delivery.addEffect(caster -> {
            if (!(caster.getWorld() instanceof ServerWorld world)) return;

            BlockPos center = caster.getBlockPos().down();
            int powerLevel = (int) (delivery.getPowerMultiplier() - 1.0f);
            int scatterSize = delivery.getScatterSize();

            if (scatterSize > 0) {
                // Apply in a 3x3 area around the caster
                BlockPos min = center.add(-scatterSize, 0, -scatterSize);
                BlockPos max = center.add(scatterSize, 0, scatterSize);
                for (BlockPos pos : BlockPos.iterate(min, max)) {
                    applyBoneMealEffect(world, pos, powerLevel);
                }
            } else {
                // Apply only on the block the caster is standing on
                applyBoneMealEffect(world, center, powerLevel);
            }
        });
    }

    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.addBlockHitEffect(hit -> {
            ServerWorld world = delivery.getCaster().getWorld();
            BlockPos pos = hit.getBlockPos();
            int powerLevel = (int) (delivery.getPowerMultiplier() - 1.0f);
            int scatterSize = delivery.getScatterSize();

            if (scatterSize > 0) {
                // Apply in a 3x3 area around the targeted block
                BlockPos min = pos.add(-scatterSize, -scatterSize, -scatterSize);
                BlockPos max = pos.add(scatterSize, scatterSize, scatterSize);
                for (BlockPos targetPos : BlockPos.iterate(min, max)) {
                    applyBoneMealEffect(world, targetPos, powerLevel);
                }
            } else {
                // Apply only on the targeted block
                applyBoneMealEffect(world, pos, powerLevel);
            }
        });
    }

    @Override
    public void apply(AoeSpellDelivery delivery) {
        delivery.addEffect(pos -> {
            ServerWorld world = delivery.getCasterWorld();
            int powerLevel = (int) (delivery.getPowerMultiplier() - 1.0f);
            applyBoneMealEffect(world, pos, powerLevel);
        });

        // Handle scatter modifier for AOE
        int scatterSize = delivery.getScatterSize();
        if (scatterSize > 0) {
            float baseRadius = delivery.getRadius();
            delivery.setRadius(baseRadius + scatterSize * 2.0f); // Increase radius by 2 blocks per scatter level
        }
    }

    private void applyBoneMealEffect(ServerWorld world, BlockPos pos, int powerLevel) {
        BlockState state = world.getBlockState(pos);

        // Calculate applications: 1 base + 2-3 per power level
        int applications = 1;
        for (int i = 0; i < powerLevel; i++) {
            applications += 2 + world.random.nextInt(2); // Adds 2 or 3 per power level
        }

        // Apply bone meal effect multiple times
        for (int i = 0; i < applications; i++) {
            if (!applySingleBoneMeal(world, pos, state)) {
                break; // Stop if bone meal application fails
            }
        }
    }

    private boolean applySingleBoneMeal(ServerWorld world, BlockPos pos, BlockState state) {
        // Use dummy bone meal stack
        ItemStack boneMealStack = new ItemStack(Items.BONE_MEAL);

        // Apply bone meal effect
        if (BoneMealItem.useOnFertilizable(boneMealStack, world, pos)) {
            // Play effects and sounds
            world.syncWorldEvent(WorldEvents.BONE_MEAL_USED, pos, 15);
            world.playSound(null, pos, SoundEvents.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS, 1.0F, 1.0F);
            return true;
        }
        return false;
    }
}