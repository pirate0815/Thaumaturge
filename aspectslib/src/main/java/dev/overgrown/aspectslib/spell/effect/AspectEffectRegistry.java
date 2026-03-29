package dev.overgrown.aspectslib.spell.effect;

import dev.overgrown.aspectslib.AspectsLib;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Global registry mapping Aspect {@link Identifier}s to their {@link AspectEffect}
 * implementations.
 *
 * <p>This registry lives in AspectsLib so that any mod can register effects and
 * look them up through a common API. Consuming mods register their effects during
 * {@code onInitialize} – for example, Thaumaturge registers Ignis, Aqua, Aer, etc.
 *
 * <p>Use {@link AspectsLib#identifier(String)} when constructing aspect IDs so the
 * namespace is always correct and never hard-coded.
 *
 * <h3>Example registration (in a consuming mod's onInitialize):</h3>
 * <pre>{@code
 *   AspectEffectRegistry.register(AspectsLib.identifier("ignis"), new IgnisEffect());
 * }</pre>
 */
public final class AspectEffectRegistry {

    private static final Map<Identifier, AspectEffect> REGISTRY = new LinkedHashMap<>();

    private AspectEffectRegistry() {}

    // ------------------------------------------------------------------ //
    // Registration                                                       //
    // ------------------------------------------------------------------ //
    /**
     * Associates an aspect {@link Identifier} with its {@link AspectEffect}.
     *
     * @throws IllegalArgumentException if {@code aspectId} is already registered
     */
    public static void register(Identifier aspectId, AspectEffect effect) {
        Objects.requireNonNull(aspectId, "aspectId must not be null");
        Objects.requireNonNull(effect,   "effect must not be null");
        if (REGISTRY.containsKey(aspectId)) {
            throw new IllegalArgumentException("Aspect effect already registered: " + aspectId);
        }
        REGISTRY.put(aspectId, effect);
        AspectsLib.LOGGER.debug("AspectEffectRegistry: registered effect for {}", aspectId);
    }

    // ------------------------------------------------------------------ //
    // Lookup                                                             //
    // ------------------------------------------------------------------ //
    /** Returns the effect for {@code aspectId}, or {@code null} if none is registered. */
    public static AspectEffect get(Identifier aspectId) {
        return REGISTRY.get(aspectId);
    }

    /** Returns {@code true} if an effect is registered for the given aspect. */
    public static boolean has(Identifier aspectId) {
        return REGISTRY.containsKey(aspectId);
    }

    /** Returns an unmodifiable view of all registered aspect IDs. */
    public static Set<Identifier> ids() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /** Clears all registrations. Intended for unit tests only. */
    public static void clear() {
        REGISTRY.clear();
    }
}