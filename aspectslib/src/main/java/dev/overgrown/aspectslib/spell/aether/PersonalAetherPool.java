package dev.overgrown.aspectslib.spell.aether;

/**
 * Per-entity Aether pool interface, injected onto {@link net.minecraft.entity.LivingEntity}
 * via {@link dev.overgrown.aspectslib.mixin.spell.LivingEntityAetherMixin}.
 *
 * <h3>Pool model</h3>
 * Every practitioner has a Personal Aether pool separate from the chunk's
 * ambient field.  Its size grows with experience (call
 * {@link #aspectslib$setMaxPersonalAether} as the practitioner advances).
 * The pool replenishes naturally through rest, food, and proximity to
 * Aether-rich environments (handled by
 * {@link PersonalAetherManager}).
 *
 * <h3>Overdraw and Unraveling</h3>
 * Drawing the pool below {@code 0} is possible only through forced mechanisms
 * (Entity pull, emergency override).  The
 * {@link dev.overgrown.aspectslib.spell.unraveling.UnravelingTracker}
 * monitors the pool and escalates the Unraveling Stage when it remains
 * critically low for extended periods.
 *
 * <h3>Conventional pool sizes:</h3>
 * <table border="1">
 *   <tr><th>Level</th><th>Approximate Pool</th></tr>
 *   <tr><td>Untrained</td><td>10–20</td></tr>
 *   <tr><td>Novice</td><td>50–100</td></tr>
 *   <tr><td>Apprentice</td><td>100–200</td></tr>
 *   <tr><td>Journeyman</td><td>200–400</td></tr>
 *   <tr><td>Expert</td><td>400–700</td></tr>
 *   <tr><td>Master</td><td>700–1,200</td></tr>
 *   <tr><td>Grandmaster</td><td>1,200–2,000</td></tr>
 * </table>
 */
public interface PersonalAetherPool {

    /** Returns current Personal Aether, in Aether units. */
    double aspectslib$getPersonalAether();

    /** Sets current Personal Aether. Values are clamped to [0, max]. */
    void aspectslib$setPersonalAether(double amount);

    /** Returns the maximum Personal Aether capacity. */
    double aspectslib$getMaxPersonalAether();

    /** Sets the maximum capacity (use when the practitioner advances). */
    void aspectslib$setMaxPersonalAether(double max);

    // Convenience helpers
    /**
     * Returns {@code current / max} in [0.0, 1.0]; returns 0 when max is 0.
     */
    default double aspectslib$getPoolFraction() {
        double max = aspectslib$getMaxPersonalAether();
        return max <= 0 ? 0.0 : Math.min(1.0, aspectslib$getPersonalAether() / max);
    }

    /**
     * Returns the named pool state:
     * <ul>
     *   <li>{@code NOMINAL}  — 25 % or more remaining</li>
     *   <li>{@code DEPLETED} — 10–25 % remaining</li>
     *   <li>{@code CRITICAL} — 1–10 % remaining</li>
     *   <li>{@code EXHAUSTED} — 0 % remaining</li>
     * </ul>
     */
    default PoolState aspectslib$getPoolState() {
        double f = aspectslib$getPoolFraction();
        if (f >= 0.25) return PoolState.NOMINAL;
        if (f >= 0.10) return PoolState.DEPLETED;
        if (f > 0.0)   return PoolState.CRITICAL;
        return PoolState.EXHAUSTED;
    }

    /**
     * Attempts to draw {@code amount} from the Personal Aether pool.
     *
     * @return {@code true} if the pool had enough and the draw succeeded;
     *         {@code false} if the pool was empty (overdraw not attempted)
     */
    default boolean aspectslib$drawPersonalAether(double amount) {
        double current = aspectslib$getPersonalAether();
        if (current < amount) return false;
        aspectslib$setPersonalAether(current - amount);
        return true;
    }

    /**
     * Restores {@code amount} Aether to the pool, clamped to max.
     */
    default void aspectslib$restorePersonalAether(double amount) {
        double current = aspectslib$getPersonalAether();
        double max     = aspectslib$getMaxPersonalAether();
        aspectslib$setPersonalAether(Math.min(max, current + amount));
    }

    // Inner enum
    enum PoolState {
        /** ≥25 % — full casting ability. */
        NOMINAL,
        /** 10–25 % — Aspect Fatigue begins: exhaustion, reduced acuity. */
        DEPLETED,
        /** 1–10 % — Spells unreliable; misfire risk increased. */
        CRITICAL,
        /** 0 % — Cannot cast voluntarily; ambient draw only; Unraveling risk. */
        EXHAUSTED
    }
}