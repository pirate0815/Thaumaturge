package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/**
 * Gelum (Frost) — Deals projectile damage, applies Slowness, freezes Water into Frosted Ice.
 */
public class GelumEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("gelum");

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
        int slowDuration = (int) (100 * ctx.potencyMult()); // 5 seconds
        int slowAmplifier = (int) Math.min(2, ctx.potencyMult());

        for (Entity target : ctx.entityTargets()) {
            if (target instanceof LivingEntity living) {
                living.damage(ctx.world().getDamageSources().magic(), damage);
                living.addStatusEffect(new StatusEffectInstance(
                        StatusEffects.SLOWNESS, slowDuration, slowAmplifier));
                acted = true;

                // Freeze nearby water blocks around the target
                BlockPos center = target.getBlockPos();
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        BlockPos pos = center.add(dx, -1, dz);
                        if (ctx.world().getBlockState(pos).isOf(Blocks.WATER)) {
                            ctx.world().setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState());
                        }
                    }
                }
            }
        }

        // Freeze water blocks at aimed location if no entity targets
        if (!acted && ctx.hasBlockTargets()) {
            for (BlockPos target : ctx.blockTargets()) {
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        BlockPos pos = target.add(dx, 0, dz);
                        if (ctx.world().getBlockState(pos).isOf(Blocks.WATER)) {
                            ctx.world().setBlockState(pos, Blocks.FROSTED_ICE.getDefaultState());
                            acted = true;
                        }
                    }
                }
            }
        }

        return acted;
    }
}
