/**
 * ScreenHandlerMixin.java
 * <p>
 * This mixin adds functionality to the Minecraft screen handler for interacting
 * with gauntlets and foci in the inventory.
 * <p>
 * It allows:
 * 1. Adding foci to gauntlets by clicking with foci on a gauntlet
 * 2. Ejecting all foci from a gauntlet by shift-right-clicking on it
 * <p>
 * This is essential to the spell system as it enables players to equip their
 * gauntlets with different foci to access different spells.
 *
 * @see dev.overgrown.thaumaturge.component.GauntletComponent
 * @see dev.overgrown.thaumaturge.component.ModComponents#GAUNTLET_STATE
 * @see dev.overgrown.thaumaturge.component.ModComponents#MAX_FOCI
 */
package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.utils.ModTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Final
    @Shadow
    public DefaultedList<Slot> slots;

    @Shadow
    public abstract ItemStack getCursorStack();

    /**
     * Intercepts slot clicks to handle interactions between gauntlets and foci
     *
     * @param slotIndex The clicked slot index
     * @param button The mouse button used (0 = left, 1 = right)
     * @param actionType The type of slot action
     * @param player The player performing the action
     * @param ci Callback info for potentially cancelling the vanilla behavior
     */
    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void handleGauntletInteraction(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        // Skip on client side - only handle on server
        if (player.getWorld().isClient()) {
            return;
        }

        // Validate slot index
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return;
        }

        Slot slot = slots.get(slotIndex);
        ItemStack slotStack = slot.getStack();
        ItemStack cursorStack = getCursorStack();

        // Case 1: Adding foci to gauntlet
        if (isGauntlet(slotStack)) {
            if (isFoci(cursorStack)) {
                // Get gauntlet component and check if there's room for more foci
                GauntletComponent component = slotStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                int maxFoci = slotStack.getOrDefault(ModComponents.MAX_FOCI, 0);
                if (component.fociCount() < maxFoci) {
                    // Add foci to gauntlet
                    Item item = cursorStack.getItem();
                    Identifier itemId = Registries.ITEM.getId(item);
                    List<Identifier> newFociIds = new ArrayList<>(component.fociIds());
                    newFociIds.add(itemId);
                    GauntletComponent newComponent = new GauntletComponent(newFociIds);
                    slotStack.set(ModComponents.GAUNTLET_STATE, newComponent);
                    cursorStack.decrement(1);
                    slot.markDirty();
                    ci.cancel(); // Cancel vanilla behavior
                }
            }
            // Case 2: Ejecting foci from gauntlet (Shift + Right-click)
            else if (actionType == SlotActionType.PICKUP && button == 1 && player.isSneaking()) {
                GauntletComponent component = slotStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                if (!component.fociIds().isEmpty()) {
                    // Return all foci to player's inventory or drop them
                    for (Identifier itemId : component.fociIds()) {
                        Item item = Registries.ITEM.get(itemId);
                        ItemStack fociStack = new ItemStack(item, 1);
                        if (!player.getInventory().insertStack(fociStack)) {
                            player.dropItem(fociStack, false);
                        }
                    }
                    // Reset gauntlet component
                    slotStack.set(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                    slot.markDirty();
                    ci.cancel(); // Cancel vanilla behavior
                }
            }
        }
    }

    /**
     * Checks if the given ItemStack is a gauntlet
     *
     * @param stack The ItemStack to check
     * @return true if the stack is a gauntlet (has MAX_FOCI component)
     */
    @Unique
    private boolean isGauntlet(ItemStack stack) {
        return stack.contains(ModComponents.MAX_FOCI);
    }

    /**
     * Checks if the given ItemStack is a foci
     *
     * @param stack The ItemStack to check
     * @return true if the stack is in the foci item tag
     */
    @Unique
    private boolean isFoci(ItemStack stack) {
        return stack.isIn(ModTags.Items.FOCI);
    }
}