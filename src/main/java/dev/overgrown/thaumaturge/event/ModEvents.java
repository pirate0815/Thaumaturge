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
    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.isIn(ModTags.Items.FOCI)) {
                Hand otherHand = hand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
                ItemStack gauntletStack = player.getStackInHand(otherHand);
                if (isGauntlet(gauntletStack)) {
                    GauntletComponent component = gauntletStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                    Integer maxFoci = gauntletStack.getOrDefault(ModComponents.MAX_FOCI, 0);
                    if (component.fociCount() < maxFoci) {
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
            } else if (player.isSneaking()) {
                ItemStack gauntletStack = player.getStackInHand(hand);
                if (isGauntlet(gauntletStack)) {
                    GauntletComponent component = gauntletStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                    if (!component.fociIds().isEmpty()) {
                        for (Identifier itemId : component.fociIds()) {
                            Item item = Registries.ITEM.get(itemId);
                            ItemStack fociStack = new ItemStack(item, 1);
                            if (!player.getInventory().insertStack(fociStack)) {
                                player.dropItem(fociStack, false);
                            }
                        }
                        gauntletStack.set(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                        return ActionResult.SUCCESS;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    private static boolean isGauntlet(ItemStack stack) {
        return stack.contains(ModComponents.MAX_FOCI);
    }
}