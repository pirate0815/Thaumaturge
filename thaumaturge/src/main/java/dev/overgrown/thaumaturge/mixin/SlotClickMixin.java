package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import dev.overgrown.thaumaturge.item.modifier.ResonanceModifierItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenHandler.class)
public abstract class SlotClickMixin {
    @Inject(method = "internalOnSlotClick", at = @At("HEAD"), cancellable = true)
    private void handleSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (slotIndex < 0 || actionType != SlotActionType.PICKUP) return;

        ScreenHandler handler = (ScreenHandler) (Object) this;
        Slot slot = handler.getSlot(slotIndex);
        ItemStack cursorStack = handler.getCursorStack();
        ItemStack slotStack = slot.getStack();

        // Handle modifier application
        if (cursorStack.getItem() instanceof ResonanceModifierItem modifierItem &&
                slotStack.getItem() instanceof FocusItem focusItem) {

            // Convert string to Identifier
            Identifier modifierId = Thaumaturge.identifier(modifierItem.getModifierType());

            focusItem.setModifier(slotStack, modifierId);
            cursorStack.decrement(1);
            ci.cancel();
            return; // Important to return after handling
        }

        // Handle focus insertion into gauntlet - BOTH DIRECTIONS
        ResonanceGauntletItem gauntletItem = null;
        ItemStack gauntletStack = null;
        ItemStack focusStack = null;

        if (cursorStack.getItem() instanceof ResonanceGauntletItem) {
            gauntletItem = (ResonanceGauntletItem) cursorStack.getItem();
            gauntletStack = cursorStack;
            focusStack = slotStack;
        } else if (slotStack.getItem() instanceof ResonanceGauntletItem) {
            gauntletItem = (ResonanceGauntletItem) slotStack.getItem();
            gauntletStack = slotStack;
            focusStack = cursorStack;
        }

        // Check if we have valid stacks to work with
        if (gauntletItem != null && focusStack.getItem() instanceof FocusItem) {
            if (gauntletItem.addFocus(gauntletStack, focusStack)) {
                if (cursorStack.getItem() instanceof FocusItem) {
                    cursorStack.decrement(1);
                } else {
                    slot.setStack(ItemStack.EMPTY);
                }
                ci.cancel();
            }
        }
    }
}