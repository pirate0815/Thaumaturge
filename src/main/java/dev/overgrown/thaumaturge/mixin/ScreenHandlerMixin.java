package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.component.FociComponent;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.utils.ModTags;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
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
import java.util.Objects;

import static dev.overgrown.thaumaturge.spell.SpellHandler.getFociTier;

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
        if (player.getWorld().isClient()) return;
        if (slotIndex < 0 || slotIndex >= slots.size()) return;

        Slot slot = slots.get(slotIndex);
        ItemStack slotStack = slot.getStack();
        ItemStack cursorStack = getCursorStack();

        // Case 1: Adding foci to gauntlet
        if (isGauntlet(slotStack)) {
            if (isFoci(cursorStack)) {
                handleFociInsertion(slotStack, cursorStack, player, ci);
            }
            // Case 2: Ejecting foci from gauntlet (Shift + Right-click)
            else if (actionType == SlotActionType.PICKUP && button == 1 && player.isSneaking()) {
                GauntletComponent component = slotStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                RegistryWrapper.WrapperLookup registries = Objects.requireNonNull(player.getServer()).getRegistryManager();
                component.entries().forEach(entry -> {
                    ItemStack stack = ItemStack.fromNbt(registries, entry.nbt()).orElse(ItemStack.EMPTY);
                    if (!stack.isEmpty()) {
                        player.getInventory().offerOrDrop(stack);
                    }
                });
                // Reset gauntlet state
                slotStack.set(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
                ci.cancel(); // Prevent default handling
            }
        }
    }

    @Unique
    private void handleFociInsertion(ItemStack gauntletStack, ItemStack fociStack, PlayerEntity player, CallbackInfo ci) {
        FociComponent fociComp = fociStack.get(ModComponents.FOCI_COMPONENT);
        SpellCastPacket.SpellTier tier = getFociTier(fociStack.getItem());

        if (fociComp != null && tier != null) {
            GauntletComponent component = gauntletStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
            Integer maxFoci = gauntletStack.getOrDefault(ModComponents.MAX_FOCI, 0);

            if (component.fociCount() < maxFoci) {
                RegistryWrapper.WrapperLookup registries = Objects.requireNonNull(player.getServer()).getRegistryManager();
                GauntletComponent.FociEntry entry = GauntletComponent.FociEntry.fromItemStack(fociStack, registries);

                // Get modifier from offhand
                ItemStack offhandStack = player.getOffHandStack();
                Identifier modifierId = getModifierId(offhandStack);

                // Create entry with modifier
                GauntletComponent.FociEntry modifiedEntry = new GauntletComponent.FociEntry(
                        entry.tier(),
                        entry.aspectId(),
                        modifierId != null ? modifierId : Thaumaturge.identifier("simple"),
                        entry.nbt()
                );

                // Consume modifier if used
                if (modifierId != null) {
                    offhandStack.decrement(1);
                }

                // Update gauntlet component
                List<GauntletComponent.FociEntry> entries = new ArrayList<>(component.entries());
                entries.add(modifiedEntry);
                gauntletStack.set(ModComponents.GAUNTLET_STATE, new GauntletComponent(entries));
                fociStack.decrement(1);
                ci.cancel();
            }
        }
    }

    @Unique
    private void handleModifierApplication(ItemStack gauntletStack, ItemStack modifierStack, PlayerEntity player, CallbackInfo ci) {
        GauntletComponent component = gauntletStack.getOrDefault(ModComponents.GAUNTLET_STATE, GauntletComponent.DEFAULT);
        Identifier modifierId = getModifierId(modifierStack);

        if (modifierId != null && !component.entries().isEmpty()) {
            // Get last inserted foci entry
            GauntletComponent.FociEntry lastEntry = component.entries().get(component.entries().size() - 1);

            // Create new entry with updated modifier
            GauntletComponent.FociEntry modifiedEntry = new GauntletComponent.FociEntry(
                    lastEntry.tier(),
                    lastEntry.aspectId(),
                    modifierId,
                    lastEntry.nbt()
            );

            // Update component
            List<GauntletComponent.FociEntry> entries = new ArrayList<>(component.entries());
            entries.set(entries.size() - 1, modifiedEntry);
            gauntletStack.set(ModComponents.GAUNTLET_STATE, new GauntletComponent(entries));

            // Consume modifier
            modifierStack.decrement(1);
            ci.cancel();
        }
    }

    @Unique
    private boolean isModifier(ItemStack stack) {
        return stack.isIn(ModTags.Items.RESONANCE_MODIFIERS);
    }

    @Unique
    private Identifier getModifierId(ItemStack stack) {
        if (isModifier(stack)) {
            return Registries.ITEM.getId(stack.getItem());
        }
        return null;
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
        return stack.contains(ModComponents.FOCI_COMPONENT);
    }
}