package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.util.Identifier;

/**
 * Chain modifier: The spell bounces to additional targets.
 * Spells that support chaining should check for this modifier and use
 * {@link #getMaxBounces()} and {@link #getMaxRange()} to determine behavior.
 */
public final class ChainModifier implements SpellModifier {

    private final int maxBounces;
    private final double maxRange;

    @Override
    public Identifier getId() {
        return AspectsLib.identifier("chain");
    }

    public ChainModifier() {
        this(3, 8.0);
    }

    public ChainModifier(int maxBounces, double maxRange) {
        this.maxBounces = Math.max(0, maxBounces);
        this.maxRange = Math.max(1, maxRange);
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        // Slightly reduce potency per bounce (optional, can be handled in spell)
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {}

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {}

    public int getMaxBounces() {
        return maxBounces;
    }
    public double getMaxRange() {
        return maxRange;
    }
}