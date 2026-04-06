package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/**
 * Perditio (Break) — Breaks the hit blocks.
 */
public class PerditioEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("perditio");

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

        // Break the block the caster is aiming at
        if (ctx.hasBlockTargets()) {
            for (BlockPos pos : ctx.blockTargets()) {
                BlockState state = ctx.world().getBlockState(pos);
                if (!state.isAir()) {
                    float hardness = state.getHardness(ctx.world(), pos);
                    if (hardness >= 0 && hardness <= 50.0f) {
                        ctx.world().breakBlock(pos, true, ctx.caster());
                        acted = true;
                        break;
                    }
                }
            }
        }

        return acted;
    }
}
