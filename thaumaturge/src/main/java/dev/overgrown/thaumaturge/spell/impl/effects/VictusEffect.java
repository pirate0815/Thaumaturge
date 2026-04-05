package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Victus (Heal) — Heals the target, damaging undead targets instead.
 */
public class VictusEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("victus");

    @Override
    public Identifier getAspectId() {
        return ID;
    }

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
        float amount = (float) (6.0 * ctx.potencyMult());

        for (Entity target : ctx.entityTargets()) {
            if (target instanceof LivingEntity living) {
                if (living.isUndead()) {
                    // Undead take damage from healing magic
                    living.damage(ctx.world().getDamageSources().magic(), amount);
                } else {
                    living.heal(amount);
                }
                acted = true;
            }
        }

        return acted;
    }
}
