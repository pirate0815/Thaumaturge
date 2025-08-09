package dev.overgrown.thaumaturge.spell.pattern;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple runtime registry mapping Aspect IDs to their effects.
 * Backed by a static map; register during mod init.
 */
public final class AspectRegistry {
    private static final Map<Identifier, AspectEffect> REGISTRY = new HashMap<>();

    private AspectRegistry() {}

    /** Registers (or replaces) an AspectEffect for the given id. */
    public static void register(Identifier id, AspectEffect effect) {
        if (id == null || effect == null) {
            throw new IllegalArgumentException("AspectRegistry.register: id and effect must be non-null");
        }
        AspectEffect prev = REGISTRY.put(id, effect);
        if (prev != null) {
            Thaumaturge.LOGGER.warn("AspectRegistry: replaced existing aspect mapping for {}", id);
        }
    }

    /** Gets the AspectEffect for the id, or null if not registered. */
    public static AspectEffect get(Identifier id) {
        return REGISTRY.get(id);
    }

    /** True if an aspect is registered for the id. */
    public static boolean isRegistered(Identifier id) {
        return REGISTRY.containsKey(id);
    }

    /** Immutable view of all registered ids (useful for debugging). */
    public static Set<Identifier> ids() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /** Clears all registrations (intended for development hot-reload/testing). */
    public static void clear() {
        REGISTRY.clear();
    }
}
