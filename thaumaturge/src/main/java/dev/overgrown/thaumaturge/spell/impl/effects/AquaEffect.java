package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Map;

/**
 * Aqua (Water) — Extinguishes fire on targets and hydrates the environment.
 *
 * <p>On entities: extinguishes burning targets and applies a small heal
 * (Aqua is a parent of Victus — water sustains life).
 * <p>On blocks (self/lesser cast): hydrates nearby farmland and
 * extinguishes fire blocks in a small radius.
 */
public class AquaEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("aqua");

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
        return SpellRange.NEAR;
    }

    @Override
    public SpellDuration getDefaultDuration() {
        return SpellDuration.INSTANT;
    }

    @Override
    public boolean apply(GauntletCastContext ctx) {
        boolean acted = false;
        float healAmount = (float) (3.0 * ctx.potencyMult());
        int radius = (int) Math.max(1, 3 * ctx.potencyMult());

        // Entity effects: extinguish + minor heal
        for (Entity target : ctx.entityTargets()) {
            if (target instanceof LivingEntity living) {
                if (living.isOnFire()) {
                    living.extinguish();
                    acted = true;
                }
                // Aqua provides a gentle heal (weaker than Victus)
                if (living.getHealth() < living.getMaxHealth()) {
                    living.heal(healAmount);
                    acted = true;
                }
            }
        }

        // Block effects for self-cast: hydrate farmland and extinguish fires
        if (ctx.focusTier().equals("lesser")) {
            ServerPlayerEntity caster = ctx.caster();
            BlockPos center = caster.getBlockPos();
            acted |= hydrateArea(ctx, center, radius);
        }

        return acted;
    }

    private boolean hydrateArea(GauntletCastContext ctx, BlockPos center, int radius) {
        boolean acted = false;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    BlockPos pos = center.add(dx, dy, dz);
                    BlockState state = ctx.world().getBlockState(pos);

                    // Extinguish fire blocks
                    if (state.isOf(Blocks.FIRE) || state.isOf(Blocks.SOUL_FIRE)) {
                        ctx.world().removeBlock(pos, false);
                        acted = true;
                    }

                    // Hydrate farmland
                    if (state.isOf(Blocks.FARMLAND)) {
                        int moisture = state.get(FarmlandBlock.MOISTURE);
                        if (moisture < FarmlandBlock.MAX_MOISTURE) {
                            ctx.world().setBlockState(pos,
                                    state.with(FarmlandBlock.MOISTURE, FarmlandBlock.MAX_MOISTURE));
                            acted = true;
                        }
                    }
                }
            }
        }
        return acted;
    }
}
