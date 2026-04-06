package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;

import java.util.Map;

/**
 * Mortuus (Curse) — Deals magic damage and applies harmful status effects on hit entities.
 */
public class MortuusEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("mortuus");

    @Override
    public Identifier getAspectId() {
        return ID;
    }

    @Override
    public Map<Identifier, Integer> getAspectIntensities() {
        return Map.of(ID, 6);
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
        int duration = (int) (200 * ctx.potencyMult()); // 10 seconds

        for (Entity target : ctx.entityTargets()) {
            if (target instanceof LivingEntity living) {
                living.damage(ctx.world().getDamageSources().magic(), damage);
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, duration, 1));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, duration, 0));
                living.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, duration, 1));
                acted = true;
            }
        }

        return acted;
    }
}
