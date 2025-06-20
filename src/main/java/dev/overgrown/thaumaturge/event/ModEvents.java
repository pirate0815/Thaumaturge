package dev.overgrown.thaumaturge.event;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.component.FociComponent;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
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

            // Handle Foci modifier removal
            if (player.isSneaking() && isFoci(stack)) {
                FociComponent component = stack.get(ModComponents.FOCI_COMPONENT);
                if (component != null && !component.modifierId().equals(Thaumaturge.identifier("simple"))) {
                    // Return modifier item to player
                    Item modifierItem = Registries.ITEM.get(component.modifierId());
                    if (modifierItem != Items.AIR) {
                        player.giveItemStack(new ItemStack(modifierItem));
                    }

                    // Reset Foci modifier
                    stack.set(ModComponents.FOCI_COMPONENT,
                            new FociComponent(component.aspectId(), Thaumaturge.identifier("simple")));

                    return ActionResult.SUCCESS;
                }
            }

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
                            ItemStack fociStack = new ItemStack(entry.item());
                            fociStack.set(ModComponents.FOCI_COMPONENT, new FociComponent(entry.aspectId(), entry.modifierId()));
                            player.getInventory().offerOrDrop(fociStack);
                        }
                        // Reset gauntlet state
                        gauntletStack.set(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    // New Foci detection method
    private static boolean isFoci(ItemStack stack) {
        return stack.contains(ModComponents.FOCI_COMPONENT);
    }

    // Existing gauntlet detection method
    private static boolean isGauntlet(ItemStack stack) {
        return stack.contains(ModComponents.MAX_FOCI);
    }
}