package dev.overgrown.thaumaturge.spell.component;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Global registry mapping Aspect {@link Identifier}s to their
 * {@link GauntletSpellEffect} implementations.
 *
 * <p>Register effects during mod init (after AspectsLib):
 * <pre>{@code
 * GauntletSpellEffectRegistry.register(new IgnisGauntletEffect());
 * GauntletSpellEffectRegistry.register(new AquaGauntletEffect());
 * }</pre>
 *
 * <p>The gauntlet resolves the active effect by calling
 * {@link #get(Identifier)} with the Aspect ID stored in the active Focus.
 */
public final class GauntletSpellEffectRegistry {

    private static final Map<Identifier, GauntletSpellEffect> REGISTRY = new LinkedHashMap<>();

    private GauntletSpellEffectRegistry() {}

    /**
     * Registers an effect. The effect's {@link GauntletSpellEffect#getAspectId()} is used as the key.
     *
     * @throws IllegalArgumentException if the aspect ID is already registered
     */
    public static void register(GauntletSpellEffect effect) {
        Objects.requireNonNull(effect, "effect must not be null");
        Identifier id = Objects.requireNonNull(effect.getAspectId(), "getAspectId() must not return null");
        if (REGISTRY.containsKey(id)) {
            throw new IllegalArgumentException("Effect already registered for aspect: " + id);
        }
        REGISTRY.put(id, effect);
        Thaumaturge.LOGGER.debug("[GauntletSpellEffectRegistry] Registered effect for {}", id);
    }

    /** Returns the effect for {@code aspectId}, or empty if none is registered. */
    public static Optional<GauntletSpellEffect> get(Identifier aspectId) {
        return Optional.ofNullable(REGISTRY.get(aspectId));
    }

    /** Returns {@code true} if an effect is registered for {@code aspectId}. */
    public static boolean has(Identifier aspectId) {
        return REGISTRY.containsKey(aspectId);
    }

    /** All registered effects, ordered by registration time. */
    public static Collection<GauntletSpellEffect> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /** All registered aspect IDs. */
    public static Set<Identifier> aspectIds() {
        return Collections.unmodifiableSet(REGISTRY.keySet());
    }
}