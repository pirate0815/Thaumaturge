package dev.overgrown.aspectslib.spell.notation;

import dev.overgrown.aspectslib.spell.SpellContext;

import java.util.List;
import java.util.function.Predicate;

/**
 * Represents the {@code ⊥[condition]} termination condition in Aspect Notation.
 *
 * <p>Every spell with a duration beyond {@code INSTANT} <strong>must</strong> have
 * at least one {@link TerminationCondition}, per Law III (The Termination Principle).
 * A {@link dev.overgrown.aspectslib.spell.Spell} with no termination condition
 * attached is treated as structurally unsound and refused by the validator.
 *
 * <h3>Built-in factory conditions</h3>
 * <ul>
 *   <li>{@link #onDurationElapsed(int)} — the spell ends after N ticks</li>
 *   <li>{@link #onCasterDeath()} — terminates if the caster dies</li>
 *   <li>{@link #onNoTargetsRemaining()} — terminates when all targets are gone</li>
 *   <li>{@link #onWorldUnloaded()} — terminates if the world is unloaded</li>
 *   <li>{@link #onAnyOf(TerminationCondition...)} — terminates when any sub-condition is met (OR)</li>
 *   <li>{@link #onAllOf(TerminationCondition...)} — terminates when all sub-conditions are met (AND)</li>
 *   <li>{@link #custom(String, Predicate)} — arbitrary predicate with a description</li>
 * </ul>
 *
 * <h3>Usage in a Spell subclass</h3>
 * <pre>{@code
 * public TerminationCondition createTerminationCondition() {
 *     // Terminate after 200 ticks OR when caster dies — whichever first
 *     return TerminationCondition.onAnyOf(
 *         TerminationCondition.onDurationElapsed(200),
 *         TerminationCondition.onCasterDeath()
 *     );
 * }
 * }</pre>
 */
public final class TerminationCondition {

    private final String description;
    private final Predicate<TerminationContext> predicate;

    private TerminationCondition(String description, Predicate<TerminationContext> predicate) {
        this.description = description;
        this.predicate   = predicate;
    }

    /**
     * Evaluates this condition given the current {@link TerminationContext}.
     *
     * @return {@code true} if the spell should end
     */
    public boolean shouldTerminate(TerminationContext ctx) {
        return predicate.test(ctx);
    }

    /** A human-readable description of this condition (for debugging). */
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "⊥[" + description + "]";
    }

    /**
     * Terminates the spell after {@code durationTicks} have elapsed since cast.
     */
    public static TerminationCondition onDurationElapsed(int durationTicks) {
        return new TerminationCondition(
                "duration_elapsed[" + durationTicks + "]",
                ctx -> ctx.ticksElapsed() >= durationTicks
        );
    }

    /** Terminates the spell when the caster is dead or no longer valid. */
    public static TerminationCondition onCasterDeath() {
        return new TerminationCondition(
                "caster_dead",
                ctx -> !ctx.spellContext().getCaster().isAlive()
        );
    }

    /** Terminates the spell when all entity targets have died or despawned. */
    public static TerminationCondition onNoTargetsRemaining() {
        return new TerminationCondition(
                "no_targets_remaining",
                ctx -> ctx.spellContext().getEntityTargets().stream().noneMatch(e -> e.isAlive() && !e.isRemoved())
        );
    }

    /**
     * Terminates the spell when the caster's world becomes unavailable
     * (e.g., unloading on server stop).
     */
    public static TerminationCondition onWorldUnloaded() {
        return new TerminationCondition(
                "world_unloaded",
                ctx -> ctx.spellContext().getWorld() == null
                        || !ctx.spellContext().getWorld().getServer().isRunning()
        );
    }

    /**
     * Terminates the spell when <em>any</em> of the given conditions is true
     * (logical OR / {@code ∨} operator).
     */
    public static TerminationCondition onAnyOf(TerminationCondition... conditions) {
        List<TerminationCondition> list = List.of(conditions);
        String desc = "any_of[" + String.join(" ∨ ",
                list.stream().map(TerminationCondition::getDescription).toList()) + "]";
        return new TerminationCondition(desc, ctx -> list.stream().anyMatch(c -> c.shouldTerminate(ctx)));
    }

    /**
     * Terminates the spell when <em>all</em> of the given conditions are true
     * (logical AND / {@code ∧} operator).
     */
    public static TerminationCondition onAllOf(TerminationCondition... conditions) {
        List<TerminationCondition> list = List.of(conditions);
        String desc = "all_of[" + String.join(" ∧ ",
                list.stream().map(TerminationCondition::getDescription).toList()) + "]";
        return new TerminationCondition(desc, ctx -> list.stream().allMatch(c -> c.shouldTerminate(ctx)));
    }

    /**
     * Terminates the spell when the caster's Personal Aether pool drops below
     * the given fraction [0.0, 1.0].
     */
    public static TerminationCondition onCasterPoolBelow(double fraction) {
        return new TerminationCondition(
                "caster_pool_below[" + (int)(fraction * 100) + "%]",
                ctx -> {
                    var caster = ctx.spellContext().getCaster();
                    if (caster instanceof dev.overgrown.aspectslib.spell.aether.PersonalAetherPool pool) {
                        return pool.aspectslib$getPoolFraction() < fraction;
                    }
                    return false;
                }
        );
    }

    /**
     * Custom termination condition with an arbitrary predicate and description.
     *
     * @param description human-readable name (shown in logs and debug commands)
     * @param predicate   returns {@code true} when the spell should end
     */
    public static TerminationCondition custom(String description, Predicate<TerminationContext> predicate) {
        return new TerminationCondition(description, predicate);
    }

    /**
     * Snapshot passed to each condition during evaluation.
     *
     * @param spellContext  the live spell context (never null)
     * @param ticksElapsed  how many ticks have passed since the spell was cast
     */
    public record TerminationContext(SpellContext spellContext, int ticksElapsed) {}
}