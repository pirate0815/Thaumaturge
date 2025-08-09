package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.effect.SpellEffect;
import dev.overgrown.thaumaturge.spell.utils.SpellContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import java.util.List;

public class SelfSpellDelivery implements SpellDelivery {
    @Override
    public void cast(World world, PlayerEntity caster, List<SpellEffect> effects) {
        SpellContext context = new SpellContext(world, caster, caster, caster.getBlockPos());
        for (SpellEffect effect : effects) {
            effect.apply(context);
        }
    }
}