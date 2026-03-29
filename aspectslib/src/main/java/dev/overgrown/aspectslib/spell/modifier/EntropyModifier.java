package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

/**
 * Entropy modifier: Introduces randomness. It may increase potency at the
 * cost of stability, or add a random secondary effect (like a small explosion,
 * poison, or knockback).  The exact behavior is chosen randomly per cast.
 */
public final class EntropyModifier implements SpellModifier {

    private static final Random RANDOM = new Random();

    @Override
    public Identifier getId() {
        return AspectsLib.identifier("entropy");
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        // Either increase potency but reduce stability, or leave as‑is.
        if (RANDOM.nextBoolean()) {
            double potency = metadata.getPotency();
            metadata.set(SpellMetadata.POTENCY, potency * 1.5);
            double stability = metadata.getStability();
            metadata.set(SpellMetadata.STABILITY, stability * 0.7);
        }
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {
        // Choose a random “extra” effect and store it in context.
        int effect = RANDOM.nextInt(5);
        ctx.putData("entropy_effect", effect);
    }

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {
        if (!success) return;
        int effect = ctx.getData("entropy_effect", 0);

        ServerWorld world = (ServerWorld) ctx.getWorld();
        Vec3d origin = ctx.getCastOrigin();
        List<LivingEntity> targets = ctx.getEntityTargets().stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .toList();

        switch (effect) {
            case 0: // small explosion
                world.createExplosion(null, origin.x, origin.y, origin.z, 1.5f, false, World.ExplosionSourceType.NONE);
                break;
            case 1: // poison cloud
                targets.forEach(e -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 0)));
                break;
            case 2: // lightning strike (visual only)
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, origin.x, origin.y, origin.z, 10, 1, 1, 1, 0.1);
                break;
            case 3: // knockback
                targets.forEach(e -> {
                    Vec3d knock = e.getPos().subtract(origin).normalize().multiply(1.5);
                    e.addVelocity(knock.x, 0.2, knock.z);
                });
                break;
            case 4: // blindness
                targets.forEach(e -> e.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0)));
                break;
        }
    }
}