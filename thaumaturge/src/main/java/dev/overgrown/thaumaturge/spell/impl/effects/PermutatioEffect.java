package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Permutatio (Exchange) — Swap the caster and the target positions.
 */
public class PermutatioEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("permutatio");

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
        LivingEntity target = ctx.primaryLivingTarget();
        if (target == null) return false;

        ServerPlayerEntity caster = ctx.caster();
        Vec3d casterPos = caster.getPos();
        Vec3d targetPos = target.getPos();
        float casterYaw = caster.getYaw();
        float casterPitch = caster.getPitch();
        float targetYaw = target.getYaw();
        float targetPitch = target.getPitch();

        // Swap positions
        caster.teleport(targetPos.x, targetPos.y, targetPos.z);
        caster.setYaw(targetYaw);
        caster.setPitch(targetPitch);
        target.teleport(casterPos.x, casterPos.y, casterPos.z);
        target.setYaw(casterYaw);
        target.setPitch(casterPitch);

        return true;
    }
}
