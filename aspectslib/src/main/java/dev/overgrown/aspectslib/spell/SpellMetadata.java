package dev.overgrown.aspectslib.spell;

import net.minecraft.nbt.NbtCompound;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@code SpellMetadata} is a flexible, map-backed container for every numeric
 * stat that describes a spell: how expensive it is, how powerful, how stable,
 * and so on.  New stats can be added without changing the class; consumers just
 * agree on key names.
 *
 * <p>Well-known stat keys are exposed as string constants and convenience
 * getters/setters so callers don't need to hard-code raw strings.
 *
 * <p>Instances are mutable by design so modifiers can layer on changes, but a
 * {@link Builder} is also provided for the common "construct once, read many"
 * pattern.
 *
 * <h3>Standard stat keys</h3>
 * <ul>
 *   <li>{@value #COMPLEXITY}    – How intricate the spell is (affects Aether draw
 *       rate and misfire chance).</li>
 *   <li>{@value #POTENCY}       – Raw magical power; scales damage/effect
 *       magnitude.</li>
 *   <li>{@value #AETHER_COST}   – Resonance Units (RU) consumed from the ambient
 *       Aether when the spell fires.</li>
 *   <li>{@value #STABILITY}     – 0.0–1.0 chance the spell behaves as intended.
 *       Low stability raises backlash risk.</li>
 *   <li>{@value #RANGE}         – Maximum reach in blocks (for targeted
 *       spells).</li>
 *   <li>{@value #CAST_TIME}     – Ticks from activation to execution.</li>
 *   <li>{@value #DURATION}      – How long sustained effects last, in ticks.</li>
 *   <li>{@value #COOLDOWN}      – Ticks before the same spell can be cast
 *       again.</li>
 * </ul>
 */
public class SpellMetadata {

    // Well-known stat key constants
    public static final String COMPLEXITY  = "complexity";
    public static final String POTENCY     = "potency";
    public static final String AETHER_COST = "aether_cost";
    public static final String STABILITY   = "stability";
    public static final String RANGE       = "range";
    public static final String CAST_TIME   = "cast_time";
    public static final String DURATION    = "duration";
    public static final String COOLDOWN    = "cooldown";

    // Defaults
    public static final SpellMetadata DEFAULT = new Builder()
            .set(COMPLEXITY,  1.0)
            .set(POTENCY,     1.0)
            .set(AETHER_COST, 10.0)
            .set(STABILITY,   1.0)
            .set(RANGE,       16.0)
            .set(CAST_TIME,   0.0)
            .set(DURATION,    0.0)
            .set(COOLDOWN,    13.0)
            .build();

    // Internal storage
    private final Map<String, Double> stats;

    private SpellMetadata(Map<String, Double> stats) {
        this.stats = new HashMap<>(stats);
    }

    // Generic accessors
    /**
     * Returns the raw stat value for {@code key}, or {@code defaultValue} when
     * the key is absent.
     */
    public double get(String key, double defaultValue) {
        return stats.getOrDefault(key, defaultValue);
    }

    /** Returns the raw stat value wrapped in an Optional. */
    public Optional<Double> get(String key) {
        return Optional.ofNullable(stats.get(key));
    }

    /**
     * Sets (or replaces) a stat. Returns {@code this} for chaining.
     */
    public SpellMetadata set(String key, double value) {
        stats.put(key, value);
        return this;
    }

    /** Adjusts a stat by {@code delta}. Missing stats are treated as 0. */
    public SpellMetadata add(String key, double delta) {
        stats.merge(key, delta, Double::sum);
        return this;
    }

    /** Multiplies a stat by {@code factor}. Missing stats are treated as 1. */
    public SpellMetadata multiply(String key, double factor) {
        stats.merge(key, factor, (existing, f) -> existing * f);
        return this;
    }

    /** Returns an unmodifiable view of all stats. */
    public Map<String, Double> asMap() {
        return Collections.unmodifiableMap(stats);
    }

    // Typed convenience getters
    public double getComplexity()  { return get(COMPLEXITY,  1.0); }
    public double getPotency()     { return get(POTENCY,     1.0); }
    public double getAetherCost()  { return get(AETHER_COST, 10.0); }
    public double getStability()   { return get(STABILITY,   1.0); }
    public double getRange()       { return get(RANGE,       16.0); }
    public int    getCastTime()    { return (int) get(CAST_TIME,  0.0); }
    public int    getDuration()    { return (int) get(DURATION,   0.0); }
    public int    getCooldown()    { return (int) get(COOLDOWN,   13.0); }

    // NBT serialization
    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        stats.forEach(nbt::putDouble);
        return nbt;
    }

    public static SpellMetadata fromNbt(NbtCompound nbt) {
        Builder b = new Builder();
        for (String key : nbt.getKeys()) {
            b.set(key, nbt.getDouble(key));
        }
        return b.build();
    }

    // Copy / merge
    /** Returns a mutable copy of this metadata. */
    public SpellMetadata copy() {
        return new SpellMetadata(stats);
    }

    /**
     * Returns a new {@code SpellMetadata} that is this object with every entry
     * from {@code overlay} layered on top (overlay values win on conflict).
     */
    public SpellMetadata mergeWith(SpellMetadata overlay) {
        SpellMetadata result = copy();
        overlay.stats.forEach(result.stats::put);
        return result;
    }

    /**
     * <b>Mutates</b> this instance by importing every stat from {@code source},
     * with source values winning on conflict.
     *
     * <p>This is the in place counterpart to {@link #mergeWith}.  Use it when
     * you need to push modifier-adjusted stats back into an existing
     * {@code SpellMetadata} reference held by a {@link dev.overgrown.aspectslib.spell.SpellContext}
     * without replacing the object itself.
     *
     * <pre>{@code
     * ctx.getMetadata().importFrom(workingMeta); // modifier results now visible
     * }</pre>
     *
     * @return {@code this} for chaining
     */
    public SpellMetadata importFrom(SpellMetadata source) {
        source.stats.forEach(this.stats::put);
        return this;
    }

    // Builder

    public static final class Builder {
        private final Map<String, Double> stats = new HashMap<>();

        public Builder() {}

        public Builder(SpellMetadata base) {
            stats.putAll(base.stats);
        }

        public Builder set(String key, double value) {
            stats.put(key, value);
            return this;
        }

        public Builder complexity(double v)  { return set(COMPLEXITY,  v); }
        public Builder potency(double v)     { return set(POTENCY,     v); }
        public Builder aetherCost(double v)  { return set(AETHER_COST, v); }
        public Builder stability(double v)   { return set(STABILITY,   v); }
        public Builder range(double v)       { return set(RANGE,       v); }
        public Builder castTime(int v)       { return set(CAST_TIME,   v); }
        public Builder duration(int v)       { return set(DURATION,    v); }
        public Builder cooldown(int v)       { return set(COOLDOWN,    v); }

        public SpellMetadata build() {
            return new SpellMetadata(stats);
        }
    }

    @Override
    public String toString() {
        return "SpellMetadata" + stats;
    }
}