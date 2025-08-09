package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.effect.SpellEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import java.util.List;

public interface SpellDelivery {
    void cast(World world, PlayerEntity caster, List<SpellEffect> effects);
}