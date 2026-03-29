package dev.overgrown.aspectslib.spell;

import dev.overgrown.aspectslib.AspectsLib;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Global registry that maps {@link Identifier} keys to {@link Spell} instances.
 *
 * <h3>Registration (mod init, before first cast)</h3>
 * <pre>{@code
 * SpellRegistry.register(IgnisSpell.ID, new IgnisSpell());
 * SpellRegistry.register(AquaSpell.ID,  new AquaSpell());
 * }</pre>
 *
 * <h3>Lookup</h3>
 * <pre>{@code
 * Optional<Spell> spell = SpellRegistry.get(new Identifier("mymod", "ignis"));
 * spell.ifPresent(s -> s.cast(ctx));
 * }</pre>
 */
public final class SpellRegistry {

    private static final Map<Identifier, Spell> REGISTRY = new LinkedHashMap<>();

    private SpellRegistry() {}

    // Registration
    /**
     * Registers a spell.  The spell's own {@link Spell#getId()} is used as the
     * canonical key; the explicit {@code id} parameter must match it.
     *
     * @throws IllegalArgumentException if {@code id} does not match {@code spell.getId()},
     *                                  or if the id is already registered
     */
    public static void register(Identifier id, Spell spell) {
        Objects.requireNonNull(id,    "Spell id must not be null");
        Objects.requireNonNull(spell, "Spell must not be null");
        if (!id.equals(spell.getId())) {
            throw new IllegalArgumentException(
                    "Registration id " + id + " does not match spell.getId() " + spell.getId());
        }
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Spell already registered: " + id);
        }
        REGISTRY.put(id, spell);
        AspectsLib.LOGGER.debug("Registered spell: {}", id);
    }

    /**
     * Convenience overload — uses {@link Spell#getId()} as the key.
     *
     * @throws IllegalArgumentException if the spell's id is already registered
     */
    public static void register(Spell spell) {
        register(spell.getId(), spell);
    }

    // Queries
    /** Returns the spell for the given id, or empty if not found. */
    public static Optional<Spell> get(Identifier id) {
        return Optional.ofNullable(REGISTRY.get(id));
    }

    /** Returns {@code true} if a spell with the given id is registered. */
    public static boolean contains(Identifier id) {
        return REGISTRY.containsKey(id);
    }

    /**
     * Returns an unmodifiable view of all registered spells, ordered by
     * registration time.
     */
    public static Collection<Spell> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /** Returns an unmodifiable view of all registered spell ids. */
    public static Set<Identifier> ids() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }

    /** Clears all registrations (intended for use in tests only). */
    public static void clear() {
        REGISTRY.clear();
    }
}