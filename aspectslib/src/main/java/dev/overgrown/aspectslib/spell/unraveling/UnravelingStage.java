package dev.overgrown.aspectslib.spell.unraveling;

/**
 * The five progressive stages of Unraveling.
 *
 * <p>Unraveling occurs when a practitioner repeatedly draws their Personal
 * Aether pool below safe thresholds without adequate recovery.  The
 * {@link UnravelingTracker} advances and retreats through these stages based on
 * the entity's pool state over time.
 *
 * <h3>Stage summary</h3>
 * <table border="1">
 *   <tr><th>Stage</th><th>Trigger</th><th>Observable effects</th></tr>
 *   <tr><td>NONE</td><td>Pool ≥ 25%</td><td>None</td></tr>
 *   <tr><td>ASPECT_BLEED</td><td>Prolonged depleted casting (weeks game-time)</td>
 *       <td>Cosmetic Aspect shifts; minor Bleed abilities; opposing Aspect degradation</td></tr>
 *   <tr><td>RESONANCE_FRACTURE</td><td>Repeated Opposing-pair forcing</td>
 *       <td>Invisible fault line; practitioners in range suffer Fatigue;
 *           risk of growing Vacuos taint</td></tr>
 *   <tr><td>ASPECT_SCREAM</td><td>Massive rapid Aether expenditure</td>
 *       <td>Aether shockwave; biological anomalies; membrane becomes vulnerable</td></tr>
 *   <tr><td>UNRAVELING</td><td>Pool drawn past recovery threshold repeatedly</td>
 *       <td>Aspect holes in signature; terminal dissolution if unchecked</td></tr>
 *   <tr><td>OPENING</td><td>Catastrophic membrane breach</td>
 *       <td>Alienis entities may cross; world-level consequences</td></tr>
 * </table>
 */
public enum UnravelingStage {

    /** No consequence; pool is healthy. */
    NONE(0, 0.0f),

    /**
     * Stage 1: Aspect Bleed.
     * Minor, individual-scale.  Dominant casting Aspect bleeds into the
     * practitioner's own Aspect composition.
     */
    ASPECT_BLEED(1, 0.15f),

    /**
     * Stage 2: Resonance Fracture.
     * Moderate, local area.  Invisible fault in the ambient Aether matrix;
     * practitioners nearby experience accelerated Fatigue.
     */
    RESONANCE_FRACTURE(2, 0.30f),

    /**
     * Stage 3: Aspect Scream.
     * Severe, community scale.  A rapid massive Aether expenditure sends a
     * pulse through the ambient network; biological and environmental anomalies.
     */
    ASPECT_SCREAM(3, 0.55f),

    /**
     * Stage 4: Unraveling.
     * Critical, individual.  The caster's own Aspects thin and may dissolve.
     * Termination of Spiritus-Humanus anchor possible if unchecked.
     */
    UNRAVELING(4, 0.80f),

    /**
     * Stage 5: Opening.
     * Catastrophic, regional. A hole in the Aether membrane; Alienis-adjacent entities may cross.
     * Requires large-scale containment.
     */
    OPENING(5, 1.00f);

    private final int level;
    /**
     * The minimum {@code stress fraction} at which this stage activates.
     * Stress is tracked by {@link UnravelingTracker} as a float in [0, 1].
     */
    private final float stressThreshold;

    UnravelingStage(int level, float stressThreshold) {
        this.level = level;
        this.stressThreshold = stressThreshold;
    }

    public int getLevel() {
        return level;
    }

    public float getStressThreshold() {
        return stressThreshold;
    }

    /**
     * Returns the stage corresponding to the given stress fraction [0, 1].
     * Selects the highest stage whose threshold is ≤ {@code stress}.
     */
    public static UnravelingStage forStress(float stress) {
        UnravelingStage result = NONE;
        for (UnravelingStage s : values()) {
            if (stress >= s.stressThreshold) result = s;
        }
        return result;
    }

    /** Returns true if this stage is more severe than {@code other}. */
    public boolean isWorseThan(UnravelingStage other) {
        return this.level > other.level;
    }

    /** Returns true if this stage is {@link #NONE}. */
    public boolean isHealthy() {
        return this == NONE;
    }
}