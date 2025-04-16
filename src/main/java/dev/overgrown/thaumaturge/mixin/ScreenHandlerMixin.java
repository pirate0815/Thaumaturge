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

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void handleGauntletInteraction(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (player.getWorld().isClient()) {
            return;
        }

        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return;
        }

        Slot slot = slots.get(slotIndex);
        ItemStack slotStack = slot.getStack();
        ItemStack cursorStack = getCursorStack();

        // Check if adding Foci to Gauntlet
        if (isGauntlet(slotStack)) {
            if (isFoci(cursorStack)) {
                GauntletComponent component = slotStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                int maxFoci = slotStack.getOrDefault(ModComponents.MAX_FOCI, 0);
                if (component.fociCount() < maxFoci) {
                    Item item = cursorStack.getItem();
                    Identifier itemId = Registries.ITEM.getId(item);
                    List<Identifier> newFociIds = new ArrayList<>(component.fociIds());
                    newFociIds.add(itemId);
                    GauntletComponent newComponent = new GauntletComponent(newFociIds);
                    slotStack.set(ModComponents.GAUNTLET_STATE, newComponent);
                    cursorStack.decrement(1);
                    slot.markDirty();
                    ci.cancel();
                }
            } else if (actionType == SlotActionType.PICKUP && button == 1 && player.isSneaking()) {
                // Eject Foci on Shift-Right-Click
                GauntletComponent component = slotStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                if (!component.fociIds().isEmpty()) {
                    for (Identifier itemId : component.fociIds()) {
                        Item item = Registries.ITEM.get(itemId);
                        ItemStack fociStack = new ItemStack(item, 1);
                        if (!player.getInventory().insertStack(fociStack)) {
                            player.dropItem(fociStack, false);
                        }
                    }
                    slotStack.set(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                    slot.markDirty();
                    ci.cancel();
                }
            }
        }
    }

    @Unique
    private boolean isGauntlet(ItemStack stack) {
        return stack.contains(ModComponents.MAX_FOCI);
    }

    @Unique
    private boolean isFoci(ItemStack stack) {
        return stack.isIn(ModTags.Items.FOCI);
    }
}