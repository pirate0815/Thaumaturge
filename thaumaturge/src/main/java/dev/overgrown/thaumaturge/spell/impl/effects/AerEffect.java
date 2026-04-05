package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Aer (Air) — Deals wind charge damage and knocks the target back.
 */
public class AerEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("aer");

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
        float damage = (float) (3.0 * ctx.potencyMult());
        double knockback = 1.5 * ctx.potencyMult();

        for (Entity target : ctx.entityTargets()) {
            if (target instanceof LivingEntity living) {
                living.damage(ctx.world().getDamageSources().magic(), damage);

                // Knockback away from caster
                Vec3d direction = target.getPos().subtract(ctx.caster().getPos()).normalize();
                living.addVelocity(
                        direction.x * knockback,
                        0.4 * ctx.potencyMult(),
                        direction.z * knockback
                );
                living.velocityModified = true;
                acted = true;
            }
        }

        return acted;
    }
}
