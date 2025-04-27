package dev.overgrown.thaumaturge.spell.combination;

import dev.overgrown.thaumaturge.effect.ModStatusEffects;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AquaGelumCombination implements SpellEntry.SpellExecutor {
    @Override
    public void execute(ServerPlayerEntity player) {
        World world = player.getWorld();

        // Apply friction effect
        player.addStatusEffect(new StatusEffectInstance(
                ModStatusEffects.ICY_FRICTION, 200, 0
        ));

        // Play snow placement sound
        BlockPos playerPos = player.getBlockPos();
        world.playSound(null, playerPos, SoundEvents.BLOCK_SNOW_PLACE, SoundCategory.PLAYERS, 1.0f, 1.0f);
    }
}