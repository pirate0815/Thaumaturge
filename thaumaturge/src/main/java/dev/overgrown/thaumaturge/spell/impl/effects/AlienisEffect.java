package dev.overgrown.thaumaturge.spell.impl.effects;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.thaumaturge.spell.component.GauntletSpellEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Map;

/**
 * Alienis (Rift) — Teleportation. Teleports the target to where the caster is looking,
 * or teleports the caster forward if self-cast.
 */
public class AlienisEffect implements GauntletSpellEffect {

    private static final Identifier ID = AspectsLib.identifier("alienis");

    @Override
    public Identifier getAspectId() {
        return ID;
    }

    @Override
    public Map<Identifier, Integer> getAspectIntensities() {
        return Map.of(ID, 6);
    }

    @Override
    public double getStabilityBase() {
        return 0.80;
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
        ServerPlayerEntity caster = ctx.caster();
        Vec3d look = caster.getRotationVec(1.0f);
        double maxDist = 16.0 * ctx.potencyMult();

        // For self-cast (lesser focus), teleport the caster forward
        if (ctx.focusTier().equals("lesser")) {
            Vec3d dest = findSafeTeleportDest(ctx, ctx.castOrigin(), look, maxDist);
            if (dest != null) {
                caster.teleport(dest.x, dest.y, dest.z);
                return true;
            }
            return false;
        }

        // For targeted cast, teleport the primary target to where the caster is looking
        LivingEntity target = ctx.primaryLivingTarget();
        if (target != null) {
            Vec3d dest = findSafeTeleportDest(ctx, ctx.castOrigin(), look, maxDist);
            if (dest != null) {
                target.teleport(dest.x, dest.y, dest.z);
                return true;
            }
        }

        return false;
    }

    private Vec3d findSafeTeleportDest(GauntletCastContext ctx, Vec3d origin, Vec3d direction, double maxDist) {
        // Walk forward along the look vector until we hit a block or reach max distance
        for (int i = (int) maxDist; i >= 1; i--) {
            Vec3d candidate = origin.add(direction.multiply(i));
            BlockPos pos = BlockPos.ofFloored(candidate);
            BlockPos above = pos.up();
            if (ctx.world().isAir(pos) && ctx.world().isAir(above)) {
                return new Vec3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            }
        }
        return null;
    }
}
