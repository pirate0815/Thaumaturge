package dev.overgrown.aspectslib.spell;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A {@code SpellPattern} represents the combination of Aspects and Modifiers
 * loaded into a casting conduit (e.g., a gauntlet or wand).  It is essentially
 * a recipe for a spell: a mapping from Aspect IDs to Modifier IDs, plus a tier
 * that defines the "power level" of the cast (e.g., lesser, advanced, greater).
 *
 * <p>The pattern is typically extracted from the conduit's NBT data by the
 * casting handler and then used to construct a {@link SpellContext}.
 *
 * <p>Instances are immutable.
 *
 * @param tier   the spell tier (e.g., "lesser", "advanced", "greater")
 * @param aspects an ordered map from Aspect {@link Identifier} to the Modifier
 *               {@link Identifier} applied to that aspect (may be {@code null}
 *               if no modifier is attached)
 */
public record SpellPattern(String tier, Map<Identifier, Identifier> aspects) {

    /**
     * Creates a new SpellPattern.
     *
     * @param tier    the spell tier; must not be null
     * @param aspects the aspect→modifier map; a copy is made to preserve immutability
     * @throws NullPointerException if tier or aspects is null
     */
    public SpellPattern {
        Objects.requireNonNull(tier, "tier must not be null");
        Objects.requireNonNull(aspects, "aspects map must not be null");
        // Defensive copy and make unmodifiable
        aspects = Collections.unmodifiableMap(new LinkedHashMap<>(aspects));
    }

    /**
     * Returns the aspect at the given position in the insertion order, or
     * {@code null} if the index is out of bounds.
     */
    public Identifier getAspectByIndex(int index) {
        if (index < 0 || index >= aspects.size()) return null;
        return aspects.keySet().stream().skip(index).findFirst().orElse(null);
    }

    /**
     * Returns the modifier for a specific aspect, or {@code null} if the aspect
     * is not present or has no modifier.
     */
    public Identifier getModifierForAspect(Identifier aspect) {
        return aspects.get(aspect);
    }

    /**
     * Checks whether this pattern contains the given aspect.
     */
    public boolean containsAspect(Identifier aspect) {
        return aspects.containsKey(aspect);
    }

    /**
     * Returns the number of aspects in the pattern.
     */
    public int size() {
        return aspects.size();
    }

    /**
     * Returns {@code true} if the pattern has no aspects.
     */
    public boolean isEmpty() {
        return aspects.isEmpty();
    }

    @Override
    public @NotNull String toString() {
        return "SpellPattern{tier=" + tier + ", aspects=" + aspects + "}";
    }
}