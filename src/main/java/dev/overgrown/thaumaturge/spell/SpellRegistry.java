/**
 * SpellRegistry.java
 * <p>
 * Central registry for all spells in the Thaumaturge mod.
 * Manages both individual spells and spell combinations.
 */
package dev.overgrown.thaumaturge.spell;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.util.Identifier;

import java.util.*;

public class SpellRegistry {
    // Maps spell identifiers to their implementations
    private static final Map<Identifier, SpellEntry> SPELLS = new HashMap<>();

    // List of all possible spell combinations
    private static final List<CombinationEntry> COMBINATIONS = new ArrayList<>();

    /**
     * Registers a spell with its unique identifier and execution logic
     *
     * @param id The unique identifier for the spell
     * @param executor The implementation that defines what happens when the spell is cast
     */
    public static void registerSpell(Identifier id, SpellEntry.SpellExecutor executor) {
        SPELLS.put(id, new SpellEntry(id, executor));
    }

    /**
     * Registers a spell combination that requires multiple foci to be present
     *
     * @param requiredComponents Set of foci identifiers required for this combination
     * @param executor The implementation of the combined spell
     */
    public static void registerCombination(Set<Identifier> requiredComponents, SpellEntry.SpellExecutor executor) {
        // Generate a unique ID for the combination based on its components
        Identifier id = Thaumaturge.identifier("combination_" + requiredComponents.hashCode());
        COMBINATIONS.add(new CombinationEntry(requiredComponents, new SpellEntry(id, executor)));
    }

    /**
     * Retrieves a spell by its identifier
     *
     * @param id The spell's unique identifier
     * @return The SpellEntry for the spell, or null if not found
     */
    public static SpellEntry getSpell(Identifier id) {
        return SPELLS.get(id);
    }

    /**
     * Finds the best spell combination for a set of equipped foci
     * "Best" is defined as the combination using the most foci
     *
     * @param equippedFoci Set of all foci identifiers currently equipped
     * @return The best matching spell combination, or null if none is found
     */
    public static SpellEntry findBestCombination(Set<Identifier> equippedFoci) {
        CombinationEntry bestMatch = null;
        for (CombinationEntry entry : COMBINATIONS) {
            if (equippedFoci.containsAll(entry.requiredComponents())) {
                // Find the combination that uses the most foci
                if (bestMatch == null || entry.requiredComponents().size() > bestMatch.requiredComponents().size()) {
                    bestMatch = entry;
                }
            }
        }
        return bestMatch != null ? bestMatch.spell() : null;
    }

    /**
     * Record that represents a spell combination with its required components and implementation
     */
    public record CombinationEntry(Set<Identifier> requiredComponents, SpellEntry spell) {}
}