package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.util.Identifier;

/**
 * Power modifier: Multiplies the spell's potency by a configurable factor
 * (default 1.5x).  The factor can be changed via constructor if a more
 * flexible implementation is needed later.
 */
public final class PowerModifier implements SpellModifier {

    private final double factor;


    @Override
    public Identifier getId() {
        return AspectsLib.identifier("power");
    }

    public PowerModifier() {
        this(1.5);
    }

    public PowerModifier(double factor) {
        this.factor = Math.max(0.1, Math.min(10.0, factor));
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        double current = metadata.getPotency();
        metadata.set(SpellMetadata.POTENCY, current * factor);
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {}

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {}
}