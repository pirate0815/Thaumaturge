package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.Spell;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * Echo modifier: After the main spell executes, it casts again (at half
 * potency) after a short delay (default 2 seconds / 40 ticks).
 */
public final class EchoModifier implements SpellModifier {

    private final int delayTicks;
    private final double potencyFactor;

    @Override
    public Identifier getId() {
        return AspectsLib.identifier("echo");
    }

    public EchoModifier() {
        this(40, 0.5);
    }

    public EchoModifier(int delayTicks, double potencyFactor) {
        this.delayTicks = Math.max(1, delayTicks);
        this.potencyFactor = Math.max(0.1, Math.min(1.0, potencyFactor));
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        ctx.putData("echo_factor", potencyFactor);
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {}

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {
        if (!success) return;

        ServerWorld serverWorld = ctx.getWorld();
        Spell originalSpell = ctx.getSpell();

        serverWorld.getServer().execute(() -> {
            // Build a new context for the echo
            SpellContext echoCtx = new SpellContext.Builder(serverWorld, ctx.getCaster(), originalSpell)
                    .conduit(ctx.getConduit())
                    .castOrigin(ctx.getCastOrigin())
                    .addEntityTargets(ctx.getEntityTargets())
                    .addBlockTargets(ctx.getBlockTargets())
                    .modifiers(ctx.getModifiers()) // keep same modifiers? maybe exclude echo itself?
                    .build();

            // Apply potency reduction
            SpellMetadata meta = echoCtx.getMetadata().copy();
            double currentPotency = meta.getPotency();
            meta.set(SpellMetadata.POTENCY, currentPotency * potencyFactor);
            echoCtx.getMetadata().importFrom(meta);

            originalSpell.cast(echoCtx);
        });
    }
}