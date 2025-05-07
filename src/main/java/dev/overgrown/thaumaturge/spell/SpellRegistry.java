package dev.overgrown.thaumaturge.spell;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class SpellRegistry {
    private static final Map<SpellCastPacket.SpellTier, Map<Set<Identifier>, SpellEntry>> tieredSpells = new HashMap<>();

    public static void registerSpell(SpellCastPacket.SpellTier tier, Set<Identifier> requiredAspectIds, SpellEntry.SpellExecutor executor) {
        Identifier spellId = Thaumaturge.identifier(
                tier.name().toLowerCase() + "_" +
                        requiredAspectIds.stream()
                                .map(Identifier::getPath)
                                .sorted()
                                .collect(Collectors.joining("_"))
        );
        SpellEntry entry = new SpellEntry(spellId, executor);
        tieredSpells.computeIfAbsent(tier, k -> new HashMap<>()).put(requiredAspectIds, entry);
    }

    public static SpellEntry findBestSpell(SpellCastPacket.SpellTier tier, Set<Identifier> equippedAspectIds) {
        Map<Set<Identifier>, SpellEntry> tierMap = tieredSpells.get(tier);
        if (tierMap == null) return null;

        SpellEntry bestEntry = null;
        int bestSize = 0;

        for (Map.Entry<Set<Identifier>, SpellEntry> entry : tierMap.entrySet()) {
            Set<Identifier> required = entry.getKey();
            if (equippedAspectIds.containsAll(required)) {
                if (required.size() > bestSize) {
                    bestEntry = entry.getValue();
                    bestSize = required.size();
                }
            }
        }
        return bestEntry;
    }
}