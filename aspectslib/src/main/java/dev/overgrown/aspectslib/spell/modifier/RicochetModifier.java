package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.util.Identifier;

/**
 * Ricochet modifier: Projectiles bounce off blocks.
 * Spells that create projectiles should check for this modifier and alter
 * their entity behavior accordingly (e.g., by setting a bounce count).
 */
public final class RicochetModifier implements SpellModifier {

    private final int maxBounces;

    @Override
    public Identifier getId() {
        return AspectsLib.identifier("ricochet");
    }

    public RicochetModifier() {
        this(3);
    }

    public RicochetModifier(int maxBounces) {
        this.maxBounces = Math.max(0, maxBounces);
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        // Could reduce range slightly because bouncing consumes energy
        double currentRange = metadata.getRange();
        metadata.set(SpellMetadata.RANGE, currentRange * 0.9);
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {}

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {}

    public int getMaxBounces() {
        return maxBounces;
    }
}