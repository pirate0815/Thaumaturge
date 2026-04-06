package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/**
 * Ignis (Fire) — Deals fire damage, sets targets on fire, can ignite blocks.
 */
public class IgnisEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("ignis");

    @Override
    public Identifier getAspectId() { return ID; }

    @Override
    public Map<Identifier, Integer> getAspectIntensities() {
        return Map.of(ID, 5);
    }

    @Override
    public SpellRange getDefaultRange() {
        return SpellRange.FAR;
    }

    @Override
    public SpellDuration getDefaultDuration() {
        return SpellDuration.INSTANT;
    }

    @Override
    public boolean apply(GauntletCastContext ctx) {
        boolean acted = false;
        float damage = (float) (4.0 * ctx.potencyMult());
        int fireTicks = (int) (100 * ctx.potencyMult()); // 5 seconds

        for (Entity target : ctx.entityTargets()) {
            if (target instanceof LivingEntity living) {
                living.damage(ctx.world().getDamageSources().onFire(), damage);
                living.setOnFireFor(fireTicks / 20);
                acted = true;
            }
        }

        // Ignite the block the caster is aiming at if no entity targets hit
        if (!acted && ctx.hasBlockTargets()) {
            for (BlockPos target : ctx.blockTargets()) {
                BlockPos above = target.up();
                ServerWorld world = ctx.world();
                if (world.isAir(above) && AbstractFireBlock.canPlaceAt(world, above, ctx.caster().getHorizontalFacing())) {
                    BlockState fireState = AbstractFireBlock.getState(world, above);
                    world.setBlockState(above, fireState);
                    acted = true;
                    break;
                }
            }
        }

        return acted;
    }
}
