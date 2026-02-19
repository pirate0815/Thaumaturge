package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.util.Identifier;

/**
 * Sustain modifier: The spell remains active over time, consuming Aether
 * periodically. The duration is stored in metadata, and the spell
 * implementation must handle the ongoing effect (e.g., by spawning an
 * entity that ticks).
 */
public final class SustainModifier implements SpellModifier {

    private final int aetherPerSecond;

    @Override
    public Identifier getId() {
        return AspectsLib.identifier("sustain");
    }

    public SustainModifier() {
        this(5);
    }

    public SustainModifier(int aetherPerSecond) {
        this.aetherPerSecond = Math.max(1, aetherPerSecond);
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        // Increase duration significantly, but also mark that it's sustained
        int baseDuration = metadata.getDuration();
        metadata.set(SpellMetadata.DURATION, Math.max(100, baseDuration * 5));
        ctx.putData("sustain_cost", aetherPerSecond);
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {}

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {
        // The actual sustain handling must be done by the spell itself,
        // e.g., by spawning an area effect cloud that ticks and deducts Aether.
    }
}