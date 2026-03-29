package dev.overgrown.aspectslib.spell.modifier;

import dev.overgrown.aspectslib.AspectsLib;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Global registry for {@link SpellModifier} implementations.
 *
 * <p>Modifiers are identified by a unique {@link Identifier} and stored as
 * singleton instances.  When a conduit stores a modifier id, the registry
 * is used to resolve the actual modifier object during spell cast.
 *
 * <p>All built‑in modifiers are registered automatically in {@link #init()};
 * external mods may register their own modifiers at any time (preferably
 * during mod initialization).
 */
public final class ModifierRegistry {

    private static final Map<Identifier, SpellModifier> REGISTRY = new LinkedHashMap<>();

    private ModifierRegistry() {}

    /**
     * Registers a built‑in modifier.  The {@code id} must be unique.
     *
     * @throws IllegalArgumentException if the id is already registered
     */
    public static void register(Identifier id, SpellModifier modifier) {
        Objects.requireNonNull(id, "Modifier id cannot be null");
        Objects.requireNonNull(modifier, "Modifier instance cannot be null");
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Modifier already registered: " + id);
        }
        REGISTRY.put(id, modifier);
        AspectsLib.LOGGER.debug("Registered spell modifier: {}", id);
    }

    /**
     * Retrieves a modifier by its identifier.
     * @return the modifier, or {@code null} if not found
     */
    public static SpellModifier get(Identifier id) {
        return REGISTRY.get(id);
    }

    /**
     * Returns an unmodifiable view of all registered modifier ids.
     */
    public static Set<Identifier> ids() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /**
     * Returns an unmodifiable collection of all registered modifiers.
     */
    public static Collection<SpellModifier> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Initializes all built‑in modifiers. Called during AspectsLib startup.
     */
    public static void init() {
        // Stable
        register(AspectsLib.identifier("stable"), new StableModifier());

        // Power – increases potency
        register(AspectsLib.identifier("power"), new PowerModifier());

        // Scatter – splits projectile into multiple rays
        register(AspectsLib.identifier("scatter"), new ScatterModifier());

        // Chain – bounces to nearby targets
        register(AspectsLib.identifier("chain"), new ChainModifier());

        // Delay – waits before executing
        register(AspectsLib.identifier("delay"), new DelayModifier());

        // Echo – re‑casts at half strength after a short delay
        register(AspectsLib.identifier("echo"), new EchoModifier());

        // Ricochet – bounces off blocks
        register(AspectsLib.identifier("ricochet"), new RicochetModifier());

        // Sustain – maintains effect over time, costing Aether per second
        register(AspectsLib.identifier("sustain"), new SustainModifier());

        // Entropy – random extra effect or misfire
        register(AspectsLib.identifier("entropy"), new EntropyModifier());

        AspectsLib.LOGGER.info("Registered {} built‑in spell modifiers", REGISTRY.size());
    }

    /** Clears all registrations (intended for testing only). */
    public static void clear() {
        REGISTRY.clear();
    }
}