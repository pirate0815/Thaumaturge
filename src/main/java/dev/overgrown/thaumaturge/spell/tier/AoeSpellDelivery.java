package dev.overgrown.thaumaturge.spell.tier;

import dev.overgrown.thaumaturge.spell.effect.SpellEffect;
import dev.overgrown.thaumaturge.spell.utils.SpellContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import java.util.List;

public class AoeSpellDelivery implements SpellDelivery {
    @Override
    public void cast(World world, PlayerEntity caster, List<SpellEffect> effects) {
        BlockPos casterPos = caster.getBlockPos();
        Box box = new Box(casterPos).expand(5);
        List<Entity> entities = world.getOtherEntities(caster, box);

        // Create context for area effect
        SpellContext areaContext = new SpellContext(world, caster, null, casterPos);
        for (SpellEffect effect : effects) {
            effect.apply(areaContext);
        }

        // Apply to entities
        for (Entity entity : entities) {
            SpellContext entityContext = new SpellContext(world, caster, entity, entity.getBlockPos());
            for (SpellEffect effect : effects) {
                effect.apply(entityContext);
            }
        }
    }
}