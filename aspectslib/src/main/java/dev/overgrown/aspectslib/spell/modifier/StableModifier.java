package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.util.Identifier;

/**
 * Stable modifier: Slightly increases stability and reduces misfire chance.
 * It does nothing else, but can be used as a "blank" modifier when no other
 * effect is desired.
 */
public final class StableModifier implements SpellModifier {

    @Override
    public Identifier getId() {
        return AspectsLib.identifier("stable");
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        // Increase stability by 10% (capped at 1.0)
        double current = metadata.getStability();
        metadata.set(SpellMetadata.STABILITY, Math.min(1.0, current + 0.1));
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {
        // Nothing special
    }

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {
        // Nothing special
    }
}