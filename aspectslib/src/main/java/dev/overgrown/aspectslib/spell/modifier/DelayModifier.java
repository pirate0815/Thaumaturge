package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.SpellMetadata;
import net.minecraft.util.Identifier;

/**
 * Delay modifier: The spell execution is postponed by a certain number of ticks.
 * The actual delay is stored in the metadata under the key
 * {@link SpellMetadata#CAST_TIME}.
 */
public final class DelayModifier implements SpellModifier {

    private final int delayTicks;

    @Override
    public Identifier getId() {
        return AspectsLib.identifier("delay");
    }

    public DelayModifier() {
        this(20); // 1 second
    }

    public DelayModifier(int delayTicks) {
        this.delayTicks = Math.max(0, delayTicks);
    }

    @Override
    public SpellMetadata modifyMetadata(SpellMetadata metadata, SpellContext ctx) {
        metadata.set(SpellMetadata.CAST_TIME, delayTicks);
        return metadata;
    }

    @Override
    public void onPreExecute(SpellContext ctx) {
        // The actual delay handling must be done by the casting system
        // (e.g., by scheduling the execution after the cast time has passed).
        // This modifier only marks the delay; the conduit or dispatcher
        // should respect SpellMetadata.CAST_TIME.
    }

    @Override
    public void onPostExecute(SpellContext ctx, boolean success) {}
}