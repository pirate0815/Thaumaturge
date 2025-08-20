package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.registry.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemUseMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void onUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack stack = user.getStackInHand(hand);
        if (stack.getItem() instanceof FocusItem focus && user.isSneaking()) {
            Identifier modifierId = focus.getModifier(stack);

            // Check if the focus has a modifier and it's not the default one
            if (!modifierId.equals(FocusItem.getDefaultModifier())) {
                String modifierType = modifierId.getPath();
                Item modifierItem = ModItems.getModifierItem(modifierType);

                if (modifierItem != null) {
                    // Create the modifier item stack
                    ItemStack modifierStack = new ItemStack(modifierItem);

                    // Remove the modifier from the focus
                    focus.setModifier(stack, FocusItem.getDefaultModifier());

                    // Give the modifier back to the player
                    if (!user.giveItemStack(modifierStack)) {
                        user.dropItem(modifierStack, false);
                    }

                    cir.setReturnValue(TypedActionResult.success(stack, world.isClient()));
                }
            }
        }
    }
}