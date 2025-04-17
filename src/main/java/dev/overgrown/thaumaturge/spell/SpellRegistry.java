package dev.overgrown.thaumaturge.spell;

import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.util.Identifier;

import java.util.*;

public class SpellRegistry {
    private static final Map<Identifier, SpellEntry> SPELLS = new HashMap<>();
    private static final List<CombinationEntry> COMBINATIONS = new ArrayList<>();

    public static void registerSpell(Identifier id, SpellEntry.SpellExecutor executor) {
        SPELLS.put(id, new SpellEntry(id, executor));
    }

    public static void registerCombination(Set<Identifier> requiredComponents, SpellEntry.SpellExecutor executor) {
        Identifier id = Identifier.of("thaumaturge", "combination_" + requiredComponents.hashCode());
        COMBINATIONS.add(new CombinationEntry(requiredComponents, new SpellEntry(id, executor)));
    }

    public static SpellEntry getSpell(Identifier id) {
        return SPELLS.get(id);
    }

    public static SpellEntry findBestCombination(Set<Identifier> equippedFoci) {
        CombinationEntry bestMatch = null;
        for (CombinationEntry entry : COMBINATIONS) {
            if (equippedFoci.containsAll(entry.requiredComponents())) {
                if (bestMatch == null || entry.requiredComponents().size() > bestMatch.requiredComponents().size()) {
                    bestMatch = entry;
                }
            }
        }
        return bestMatch != null ? bestMatch.spell() : null;
    }

    public record CombinationEntry(Set<Identifier> requiredComponents, SpellEntry spell) {}
}