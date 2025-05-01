/**
 * SpellHandler.java
 * <p>
 * Handles the actual casting of spells, checking for required foci,
 * and selecting between individual spells and combinations.
 */
package dev.overgrown.thaumaturge.spell;

import dev.overgrown.thaumaturge.component.FociComponent;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import java.util.HashSet;
import java.util.Set;

public class SpellHandler {
    public static void tryCastSpell(ServerPlayerEntity player, SpellCastPacket.SpellTier tier) {
        Set<Identifier> equippedAspectIds = getEquippedAspectIds(player, tier);
        SpellEntry entry = SpellRegistry.findBestSpell(tier, equippedAspectIds);
        if (entry != null) {
            entry.executor().execute(player);
        }
    }

    private static Set<Identifier> getEquippedAspectIds(ServerPlayerEntity player, SpellCastPacket.SpellTier tier) {
        Set<Identifier> aspectIds = new HashSet<>();
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.contains(ModComponents.MAX_FOCI)) {
                GauntletComponent gauntlet = stack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                for (Identifier fociItemId : gauntlet.fociIds()) {
                    ItemStack fociStack = new ItemStack(Registries.ITEM.get(fociItemId));
                    FociComponent component = fociStack.getOrDefault(ModComponents.FOCI_COMPONENT, FociComponent.DEFAULT);
                    if (component.tier() == tier) {
                        aspectIds.add(component.aspectId());
                    }
                }
            }
        }
        return aspectIds;
    }
}