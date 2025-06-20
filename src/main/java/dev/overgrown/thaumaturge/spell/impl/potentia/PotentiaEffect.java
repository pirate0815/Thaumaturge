package dev.overgrown.thaumaturge.spell.impl.potentia;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.utils.ModSounds;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;

public class PotentiaEffect implements AspectEffect {
    @Override
    public void apply(TargetedSpellDelivery delivery) {
        delivery.setMaxDistance(16.0);
        delivery.addOnHitEffect(entity -> {
            ServerWorld world = delivery.getCaster().getWorld();
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    ModSounds.POTENTIA_SPELL_CAST, SoundCategory.PLAYERS, 1.0f, 1.0f);
        });
    }
}