package dev.overgrown.thaumaturge.spell.modifier;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Runtime registry for spell modifiers.
 */
public final class ModifierRegistry {
    private static final Map<Identifier, ModifierEffect> REGISTRY = new HashMap<>();

    private ModifierRegistry() {}

    /** Register (or replace) a modifier implementation for the given id. */
    public static void register(Identifier id, ModifierEffect effect) {
        if (id == null || effect == null) {
            throw new IllegalArgumentException("ModifierRegistry.register: id and effect must be non-null");
        }
        ModifierEffect prev = REGISTRY.put(id, effect);
        if (prev != null) {
            Thaumaturge.LOGGER.warn("ModifierRegistry: replaced existing modifier for {}", id);
        }
    }

    /** Get the modifier by id, or null if not registered. */
    public static ModifierEffect get(Identifier id) {
        return REGISTRY.get(id);
    }

    /** True if an id is registered. */
    public static boolean isRegistered(Identifier id) {
        return REGISTRY.containsKey(id);
    }

    /** Immutable view of all registered modifier ids. */
    public static Set<Identifier> ids() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /** Clear all registrations (useful for dev reload/tests). */
    public static void clear() {
        REGISTRY.clear();
    }
}
