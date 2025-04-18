/**
 * ModEvents.java
 * <p>
 * This class registers and handles custom events for the mod.
 * Currently, implements:
 * - Using foci items directly on gauntlets to equip them
 * - Shift-using gauntlets to eject all equipped foci
 * <p>
 * These events provide alternative ways to interact with gauntlets besides
 * the inventory screen handling.
 *
 * @see dev.overgrown.thaumaturge.mixin.ScreenHandlerMixin
 * @see dev.overgrown.thaumaturge.component.GauntletComponent
 */
package dev.overgrown.thaumaturge.event;

import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.utils.ModTags;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ModEvents {
    /**
     * Registers all event handlers for the mod
     */
    public static void register() {
        // Register handler for using items
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            // Case 1: Using a foci item - try to add to gauntlet in other hand
            if (stack.isIn(ModTags.Items.FOCI)) {
                Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
                ItemStack gauntletStack = player.getStackInHand(otherHand);
                if (isGauntlet(gauntletStack)) {
                    // Check if gauntlet has room for more foci
                    GauntletComponent component = gauntletStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                    Integer maxFoci = gauntletStack.getOrDefault(ModComponents.MAX_FOCI, 0);
                    if (component.fociCount() < maxFoci) {
                        // Add foci to gauntlet
                        Item item = stack.getItem();
                        Identifier itemId = Registries.ITEM.getId(item);
                        List<Identifier> newFociIds = new ArrayList<>(component.fociIds());
                        newFociIds.add(itemId);
                        GauntletComponent newComponent = new GauntletComponent(newFociIds);
                        gauntletStack.set(ModComponents.GAUNTLET_STATE, newComponent);
                        stack.decrement(1);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            // Case 2: Shift-using a gauntlet - eject all equipped foci
            else if (player.isSneaking()) {
                ItemStack gauntletStack = player.getStackInHand(hand);
                if (isGauntlet(gauntletStack)) {
                    // Get current foci in the gauntlet
                    GauntletComponent component = gauntletStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);

                    // Only proceed if the gauntlet has foci
                    if (!component.fociIds().isEmpty()) {
                        // Return all foci items to player's inventory or drop them if inventory is full
                        for (Identifier itemId : component.fociIds()) {
                            Item item = Registries.ITEM.get(itemId);
                            ItemStack fociStack = new ItemStack(item, 1);

                            // Try to add to inventory first, drop in world if full
                            if (!player.getInventory().insertStack(fociStack)) {
                                player.dropItem(fociStack, false);
                            }
                        }

                        // Reset the gauntlet to empty state
                        gauntletStack.set(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            // If no special handling occurred, pass to default behavior
            return ActionResult.PASS;
        });
    }

    /**
     * Determines if an ItemStack is a gauntlet.
     * Gauntlets are identified by having the MAX_FOCI component.
     *
     * @param stack The ItemStack to check
     * @return true if the item is a gauntlet, false otherwise
     */
    private static boolean isGauntlet(ItemStack stack) {
        return stack.contains(ModComponents.MAX_FOCI);
    }
}