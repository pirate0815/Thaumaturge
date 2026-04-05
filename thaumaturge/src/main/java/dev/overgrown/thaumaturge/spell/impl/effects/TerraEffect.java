package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/**
 * Terra (Earth) — Deals projectile damage. Breaks weak blocks.
 */
public class TerraEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("terra");

    @Override
    public Identifier getAspectId() {
        return ID;
    }

    @Override
    public Map<Identifier, Integer> getAspectIntensities() {
        return Map.of(ID, 4);
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
        float damage = (float) (5.0 * ctx.potencyMult());

        for (Entity target : ctx.entityTargets()) {
            if (target instanceof LivingEntity living) {
                living.damage(ctx.world().getDamageSources().magic(), damage);
                acted = true;
            }
        }

        // Break weak blocks at the aimed block
        if (!acted && ctx.hasBlockTargets()) {
            for (BlockPos target : ctx.blockTargets()) {
                BlockState state = ctx.world().getBlockState(target);
                float hardness = state.getHardness(ctx.world(), target);
                if (hardness >= 0 && hardness <= 1.5f && !state.isAir()) {
                    ctx.world().breakBlock(target, true, ctx.caster());
                    acted = true;
                    break;
                }
            }
        }

        return acted;
    }
}
