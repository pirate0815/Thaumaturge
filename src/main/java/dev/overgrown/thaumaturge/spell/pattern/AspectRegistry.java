package dev.overgrown.thaumaturge.spell.pattern;

import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Simple aspect registry for server execution.
 * - Thread-safe for typical mod init + runtime reads.
 * - API matches SpellHandler's expectations (Optional-returning get).
 * <p>
 * Swap-in plan for AspectsLib:
 *  - Replace the internal map with a delegating lookup that resolves Identifier -> AspectEffect
 *    using AspectsLib data, or register adapters during init.
 */
public final class AspectRegistry {
    private static final Map<Identifier, AspectEffect> REGISTRY =
            Collections.synchronizedMap(new LinkedHashMap<>());

    private AspectRegistry() {}

    /** Registers or replaces an aspect effect under the given id. */
    public static void register(Identifier id, AspectEffect effect) {
        if (id == null || effect == null) return;
        REGISTRY.put(id, effect);
    }

    /** Fetches an aspect effect if present. */
    public static Optional<AspectEffect> get(Identifier id) {
        if (id == null) return Optional.empty();
        AspectEffect eff = REGISTRY.get(id);
        return Optional.ofNullable(eff);
    }

    /** True if an aspect id is known. */
    public static boolean contains(Identifier id) {
        return id != null && REGISTRY.containsKey(id);
    }

    /** Clears all entries (useful for data pack reloads or tests). */
    public static void clear() {
        REGISTRY.clear();
    }
}
