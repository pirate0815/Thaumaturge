package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Motus (Motion) — Applies velocity to objects or entities.
 */
public class MotusEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("motus");

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
        double strength = 2.0 * ctx.potencyMult();

        Vec3d pushDir = ctx.caster().getRotationVec(1.0f).normalize();

        for (Entity target : ctx.entityTargets()) {
            // If self-cast, launch the caster in their look direction
            if (target == ctx.caster()) {
                target.addVelocity(
                        pushDir.x * strength,
                        Math.max(0.4, pushDir.y * strength),
                        pushDir.z * strength
                );
            } else {
                // Push target away from caster
                Vec3d away = target.getPos().subtract(ctx.caster().getPos()).normalize();
                target.addVelocity(
                        away.x * strength,
                        0.3 * ctx.potencyMult(),
                        away.z * strength
                );
            }
            target.velocityModified = true;
            acted = true;
        }

        return acted;
    }
}
