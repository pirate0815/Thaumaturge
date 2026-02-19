package dev.overgrown.aspectslib.spell;

import dev.overgrown.aspectslib.resonance.Resonance;
import dev.overgrown.aspectslib.resonance.ResonanceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import java.util.*;

/**
 * {@code EnvironmentalResonance} encapsulates the dynamic interaction between
 * the aspects present in a spell and those naturally occurring in the
 * environment (the chunk's Aether).  It can either <em>amplify</em> the spell's
 * effects (when compatible aspects are abundant) or <em>neutralize</em> them
 * (when opposing aspects dominate).
 *
 * <p>This class is typically constructed by the casting system just before a
 * spell is executed, based on the spell's {@link SpellPattern} and the
 * {@link dev.overgrown.aspectslib.aether.AetherChunkData} of the casting
 * location.
 *
 * <p>Amplification and barrier (opposing) values are computed using the
 * global {@link ResonanceManager} and the actual amounts of each aspect
 * present in the chunk.  The results are exposed per aspect via methods like
 * {@link #getAmplificationFactor} and {@link #getBarrierCost}.
 *
 * @see ResonanceManager
 * @see dev.overgrown.aspectslib.resonance.ResonanceCalculator
 */
public final class EnvironmentalResonance {

    // Per‑aspect results
    private final Map<Identifier, Double> amplification = new HashMap<>();
    private final Map<Identifier, Double> barrier = new HashMap<>();

    /**
     * Creates an empty resonance (no effects).
     */
    public EnvironmentalResonance() {}

    /**
     * Creates a resonance by evaluating the interaction between the spell's
     * aspects and the environment's aspects in the given chunk.
     *
     * @param world        the world where the spell is cast
     * @param chunkPos     the chunk containing the cast origin
     * @param spellAspects the set of aspect identifiers used in the spell
     */
    public EnvironmentalResonance(World world, ChunkPos chunkPos, Collection<Identifier> spellAspects) {
        this();
        if (world.isClient()) return; // only meaningful on server

        // Get environmental aspects from the chunk's Aether data
        var aetherData = dev.overgrown.aspectslib.aether.AetherManager.getAetherData(world, chunkPos);
        if (aetherData == null) return;

        // For each spell aspect, look for resonance relationships with every
        // environmental aspect that actually has some Aether.
        for (Identifier spellAspect : spellAspects) {
            double totalAmp = 1.0;
            double totalBarrier = 0.0;

            for (Identifier envAspect : aetherData.getAspectIds()) {
                int envAmount = aetherData.getCurrentAether(envAspect);
                if (envAmount <= 0) continue;

                List<Resonance> resonances = ResonanceManager.RESONANCE_MAP.getOrDefault(spellAspect, List.of());
                for (Resonance res : resonances) {
                    if (res.matches(spellAspect, envAspect)) {
                        double factor = res.factor();
                        if (res.type() == Resonance.Type.AMPLIFYING) {
                            // Amplification grows with the amount of the environmental aspect
                            totalAmp += (envAmount * factor) / 100.0; // scaling factor; adjust as needed
                        } else { // OPPOSING
                            // Barrier cost is proportional to the amount present
                            totalBarrier += envAmount * factor;
                        }
                    }
                }
            }

            amplification.put(spellAspect, Math.max(1.0, totalAmp));
            barrier.put(spellAspect, totalBarrier);
        }
    }

    /**
     * Returns the amplification factor for the given spell aspect.
     * The result is always ≥ 1.0, where 1.0 means no amplification.
     */
    public double getAmplificationFactor(Identifier aspect) {
        return amplification.getOrDefault(aspect, 1.0);
    }

    /**
     * Returns the barrier cost (opposing resonance) for the given spell aspect.
     * This is a raw value that can be subtracted from the spell's effect
     * (e.g., damage).  If no opposing aspects are present, returns 0.
     */
    public double getBarrierCost(Identifier aspect) {
        return barrier.getOrDefault(aspect, 0.0);
    }

    /**
     * Returns true if the given spell aspect is opposed by the environment.
     */
    public boolean hasOpposition(Identifier aspect) {
        return barrier.getOrDefault(aspect, 0.0) > 0;
    }

    /**
     * Returns true if the given spell aspect is amplified by the environment.
     */
    public boolean hasAmplification(Identifier aspect) {
        return amplification.getOrDefault(aspect, 1.0) > 1.0;
    }

    /**
     * Combines this resonance with another, adding amplification factors
     * (multiplying) and summing barrier costs.  Used when multiple sources
     * of resonance need to be merged.
     */
    public EnvironmentalResonance merge(EnvironmentalResonance other) {
        EnvironmentalResonance result = new EnvironmentalResonance();
        Set<Identifier> allKeys = new HashSet<>();
        allKeys.addAll(this.amplification.keySet());
        allKeys.addAll(other.amplification.keySet());
        allKeys.addAll(this.barrier.keySet());
        allKeys.addAll(other.barrier.keySet());

        for (Identifier key : allKeys) {
            double amp = this.amplification.getOrDefault(key, 1.0)
                    * other.amplification.getOrDefault(key, 1.0);
            double barr = this.barrier.getOrDefault(key, 0.0)
                    + other.barrier.getOrDefault(key, 0.0);
            if (amp > 1.0) result.amplification.put(key, amp);
            if (barr > 0.0) result.barrier.put(key, barr);
        }
        return result;
    }

    @Override
    public String toString() {
        return "EnvironmentalResonance{amp=" + amplification + ", barrier=" + barrier + "}";
    }
}