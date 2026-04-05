package dev.overgrown.aspectslib.spell;

import dev.overgrown.aspectslib.spell.aether.PersonalAetherManager;
import dev.overgrown.aspectslib.spell.aether.PersonalAetherPool;
import dev.overgrown.aspectslib.spell.cost.AetherCostCalculator;
import dev.overgrown.aspectslib.spell.cost.ResonanceDiscountCalculator;
import dev.overgrown.aspectslib.spell.cost.SpellCostParams;
import dev.overgrown.aspectslib.spell.cost.SpellDuration;
import dev.overgrown.aspectslib.spell.cost.SpellRange;
import dev.overgrown.aspectslib.spell.modifier.ModifierRegistry;
import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import dev.overgrown.aspectslib.spell.notation.NotationFormula;
import dev.overgrown.aspectslib.spell.notation.TerminationCondition;
import dev.overgrown.aspectslib.spell.perception.AetherSightCapability;
import dev.overgrown.aspectslib.spell.perception.AetherSightData;
import dev.overgrown.aspectslib.spell.resonance.VolatileResonanceRegistry;
import dev.overgrown.aspectslib.spell.unraveling.UnravelingStage;
import dev.overgrown.aspectslib.spell.unraveling.UnravelingTracker;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Optional;

/**
 * Single-entry-point public API for AspectsLib's spell system.
 *
 * <p>Consuming mods (Thaumaturge, Witch, etc.) should interact with the spell
 * system almost entirely through this class, using the subsystem classes
 * directly only when fine-grained control is needed.
 *
 * <h3>Quick-start for implementing a spell</h3>
 * <ol>
 *   <li>Extend {@link Spell}; override {@link Spell#getId},
 *       {@link Spell#buildCostParams}, and {@link Spell#execute}.</li>
 *   <li>Call {@code SpellAPI.registerSpell(new MySpell())} during your mod's
 *       {@code onInitialize}.</li>
 *   <li>Use {@code SpellAPI.computeCost(params)} in documentation or UI to
 *       show players the expected Aether draw.</li>
 * </ol>
 *
 * <h3>Quick-start for implementing a spell modifier</h3>
 * <ol>
 *   <li>Implement {@link SpellModifier}.</li>
 *   <li>Call {@code SpellAPI.registerModifier(id, modifier)}.</li>
 * </ol>
 */
public final class SpellAPI {

    private SpellAPI() {}

    /**
     * Registers a spell. Equivalent to {@link SpellRegistry#register(Spell)}.
     *
     * @throws IllegalArgumentException if the spell's id is already registered
     */
    public static void registerSpell(Spell spell) {
        SpellRegistry.register(spell);
    }

    /** Returns the registered spell for {@code id}, or empty. */
    public static Optional<Spell> getSpell(Identifier id) {
        return SpellRegistry.get(id);
    }

    /**
     * Registers a spell modifier.
     *
     * @throws IllegalArgumentException if {@code id} is already registered
     */
    public static void registerModifier(Identifier id, SpellModifier modifier) {
        ModifierRegistry.register(id, modifier);
    }

    /**
     * Computes the full Aether cost for the given parameters.
     *
     * @see AetherCostCalculator#compute(SpellCostParams)
     */
    public static double computeCost(SpellCostParams params) {
        return AetherCostCalculator.compute(params);
    }

    /**
     * Convenience: computes cost for a simple single-Aspect spell.
     *
     * @param aspectId the Aspect being cast
     * @param intensity intensity 1–10
     * @param range range code
     * @param duration duration code
     */
    public static double computeSimpleCost(Identifier aspectId, int intensity,
                                           SpellRange range, SpellDuration duration) {
        return AetherCostCalculator.compute(
                new SpellCostParams.Builder()
                        .aspect(aspectId, intensity)
                        .range(range)
                        .duration(duration)
                        .build()
        );
    }

    /**
     * Returns the resonance discount fraction for the given Aspect set.
     *
     * @param aspectIds aspects present in the spell
     * @param atLeyNode whether the cast is at a Ley Node
     */
    public static double resonanceDiscount(Collection<Identifier> aspectIds, boolean atLeyNode) {
        return ResonanceDiscountCalculator.compute(aspectIds, atLeyNode);
    }

    /**
     * Validates a {@link NotationFormula} and returns the result.
     * Use this before inscribing a Mage spell (i.e., at mod design time or
     * in a crafting/inscription system).
     */
    public static NotationFormula.ValidationResult validateFormula(NotationFormula formula) {
        return formula.validate();
    }

    /** Returns all Volatile pairs who's both members appear in {@code aspectIds}. */
    public static java.util.List<dev.overgrown.aspectslib.spell.resonance.VolatileResonancePair>
    detectVolatilePairs(Collection<Identifier> aspectIds) {
        return VolatileResonanceRegistry.detectPairs(aspectIds);
    }

    /**
     * Returns the entity's current Personal Aether pool value.
     */
    public static double getPersonalAether(LivingEntity entity) {
        return PersonalAetherManager.getPool(entity);
    }

    /**
     * Returns the entity's pool fraction [0, 1].
     */
    public static double getPersonalAetherFraction(LivingEntity entity) {
        return PersonalAetherManager.getPoolFraction(entity);
    }

    /**
     * Sets the entity's maximum pool (e.g. when the practitioner advances a tier).
     */
    public static void setMaxPersonalAether(LivingEntity entity, double newMax) {
        PersonalAetherManager.setMaxPool(entity, newMax);
    }

    /**
     * Immediately restores {@code amount} Aether to the entity's pool.
     * Use for consumables, rest, or Artificer devices.
     */
    public static void restorePersonalAether(LivingEntity entity, double amount) {
        PersonalAetherManager.restore(entity, amount);
    }

    /**
     * Returns the entity's {@link PersonalAetherPool.PoolState}.
     */
    public static PersonalAetherPool.PoolState getPoolState(LivingEntity entity) {
        if (entity instanceof PersonalAetherPool pool) return pool.aspectslib$getPoolState();
        return PersonalAetherPool.PoolState.EXHAUSTED;
    }

    /**
     * Returns the entity's current {@link UnravelingStage}.
     */
    public static UnravelingStage getUnravelingStage(LivingEntity entity) {
        return UnravelingTracker.getStage(entity);
    }

    /**
     * Returns the entity's raw Unraveling stress (0.0 = none, 1.0 = max).
     */
    public static float getUnravelingStress(LivingEntity entity) {
        return UnravelingTracker.getStress(entity);
    }

    /**
     * Clears all Unraveling stress for the entity.
     * Use for purification rituals or recovery mechanics.
     */
    public static void clearUnravelingStress(LivingEntity entity) {
        UnravelingTracker.clearStress(entity);
    }

    /**
     * Manually records an overdraw event (for forced draws via Warlock Pact,
     * Entity pull, or emergency casts).
     *
     * @param entity the caster
     * @param overdrawFraction how far beyond empty the pool went (e.g., 0.05 = 5%)
     */
    public static void recordOverdraw(LivingEntity entity, float overdrawFraction) {
        UnravelingTracker.recordOverdraw(entity, overdrawFraction);
    }

    /**
     * Returns the entity's {@link AetherSightData}, or {@code null} if the
     * entity does not implement {@link AetherSightCapability}.
     */
    public static AetherSightData getSightData(LivingEntity entity) {
        if (entity instanceof AetherSightCapability cap) return cap.aspectslib$getSightData();
        return null;
    }

    /**
     * Attempts to activate Aether Sight for the entity.
     * Returns false if the entity's stage is too low (< 2).
     */
    public static boolean activateSight(LivingEntity entity) {
        if (entity instanceof AetherSightCapability cap) return cap.aspectslib$activateSight();
        return false;
    }

    /** Adds Staff-use practice ticks to advance the entity's Aether Sight stage. */
    public static void addSightPracticeTicks(LivingEntity entity, long ticks) {
        if (entity instanceof AetherSightCapability cap) {
            cap.aspectslib$getSightData().addPracticeTicks(ticks);
        }
    }

    /**
     * Registers a custom Volatile Resonance pair.
     * Call during {@code onInitialize} for custom Aspect combinations.
     */
    public static void registerVolatilePair(
            dev.overgrown.aspectslib.spell.resonance.VolatileResonancePair pair) {
        VolatileResonanceRegistry.register(pair);
    }

    /**
     * Creates a duration-elapsed termination condition ({@code ⊥[duration_elapsed]}).
     *
     * @param ticks number of ticks until the spell terminates
     */
    public static TerminationCondition terminateAfter(int ticks) {
        return TerminationCondition.onDurationElapsed(ticks);
    }

    /**
     * Creates a compound termination condition that fires when ANY sub-condition
     * is met ({@code ⊥[A ∨ B]}).
     */
    public static TerminationCondition terminateOnAny(TerminationCondition... conditions) {
        return TerminationCondition.onAnyOf(conditions);
    }
}