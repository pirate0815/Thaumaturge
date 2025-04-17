package dev.overgrown.thaumaturge.spell;

import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.spell.impl.aer.AdvancedAerLaunch;
import dev.overgrown.thaumaturge.spell.impl.aer.GreaterAerBurst;
import dev.overgrown.thaumaturge.spell.impl.aer.LesserAerBoost;
import dev.overgrown.thaumaturge.spell.impl.motus.LesserMotusBoost;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import java.util.HashSet;
import java.util.Set;

public class SpellHandler {
    public static void tryCastSpell(ServerPlayerEntity player, Identifier spellId) {
        Set<Identifier> equippedFoci = getEquippedFoci(player);

        // Check combinations first
        SpellEntry combination = SpellRegistry.findBestCombination(equippedFoci);
        if (combination != null) {
            combination.executor().execute(player);
            return;
        }

        // Check single spell
        SpellEntry entry = SpellRegistry.getSpell(spellId);
        if (entry != null && hasRequiredFoci(equippedFoci, spellId)) {
            entry.executor().execute(player);
        }
    }

    private static Set<Identifier> getEquippedFoci(ServerPlayerEntity player) {
        Set<Identifier> foci = new HashSet<>();
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.contains(ModComponents.MAX_FOCI)) {
                GauntletComponent component = stack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                foci.addAll(component.fociIds());
            }
        }
        return foci;
    }

    private static boolean hasRequiredFoci(Set<Identifier> equipped, Identifier spellId) {
        // Map spellId to the corresponding foci item's Identifier
        Identifier requiredFociId = getRequiredFociId(spellId);
        return requiredFociId != null && equipped.contains(requiredFociId);
    }

    private static Identifier getRequiredFociId(Identifier spellId) {
        // Define mappings between spell IDs and their required foci item IDs
        if (spellId.equals(LesserAerBoost.ID)) {
            return Registries.ITEM.getId(ModItems.LESSER_AER_FOCI);
        } else if (spellId.equals(LesserMotusBoost.ID)) {
            return Registries.ITEM.getId(ModItems.LESSER_MOTUS_FOCI);
        } else if (spellId.equals(AdvancedAerLaunch.ID)) {
            return Registries.ITEM.getId(ModItems.ADVANCED_AER_FOCI);
        } else if (spellId.equals(GreaterAerBurst.ID)) {
            return Registries.ITEM.getId(ModItems.GREATER_AER_FOCI);
        }
        return null;
    }
}