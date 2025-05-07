package dev.overgrown.thaumaturge.event;

import dev.overgrown.thaumaturge.component.FociComponent;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static dev.overgrown.thaumaturge.spell.SpellHandler.getFociTier;

public class ModEvents {
    /**
     * Registers all event handlers for the mod
     */
    public static void register() {
        // Register handler for using items
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient()) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getStackInHand(hand);

            // Case 1: Using a foci item - try to add to gauntlet in other hand
            if (stack.getComponents().contains(ModComponents.FOCI_COMPONENT)) {
                Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
                ItemStack gauntletStack = player.getStackInHand(otherHand);
                if (isGauntlet(gauntletStack)) {
                    GauntletComponent component = gauntletStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                    Integer maxFoci = gauntletStack.getOrDefault(ModComponents.MAX_FOCI, 0);

                    if (component.fociCount() < maxFoci) {
                        FociComponent fociComp = stack.get(ModComponents.FOCI_COMPONENT);
                        SpellCastPacket.SpellTier tier = getFociTier(stack.getItem());

                        if (fociComp != null && tier != null) {
                            RegistryWrapper.WrapperLookup registries = Objects.requireNonNull(player.getServer()).getRegistryManager();
                            GauntletComponent.FociEntry entry = GauntletComponent.FociEntry.fromItemStack(stack, registries);
                            List<GauntletComponent.FociEntry> entries = new ArrayList<>(component.entries());
                            entries.add(entry);
                            GauntletComponent newComponent = new GauntletComponent(entries);
                            gauntletStack.set(ModComponents.GAUNTLET_STATE, newComponent);
                            stack.decrement(1);
                            return ActionResult.SUCCESS;
                        }
                    }
                }
            }
            // Case 2: Shift-using a gauntlet - eject all equipped foci
            else if (player.isSneaking()) {
                ItemStack gauntletStack = player.getStackInHand(hand);
                if (isGauntlet(gauntletStack)) {
                    GauntletComponent component = gauntletStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);

                    if (!component.entries().isEmpty()) {
                        RegistryWrapper.WrapperLookup registries = Objects.requireNonNull(player.getServer()).getRegistryManager();
                        for (GauntletComponent.FociEntry entry : component.entries()) {
                            ItemStack fociStack = ItemStack.fromNbt(registries, entry.nbt()).orElse(ItemStack.EMPTY);
                            if (!fociStack.isEmpty()) {
                                player.giveItemStack(fociStack);
                            }
                        }
                        // Reset gauntlet state
                        gauntletStack.set(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                    }
                }
            }
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