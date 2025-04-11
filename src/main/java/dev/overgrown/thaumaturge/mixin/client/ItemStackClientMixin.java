package dev.overgrown.thaumaturge.mixin.client;

import dev.overgrown.thaumaturge.component.AspectComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import dev.overgrown.thaumaturge.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {

    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void addAspectTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        ItemStack stack = (ItemStack) (Object) this;
        AspectComponent component = stack.getOrDefault(AspectComponent.TYPE, AspectComponent.DEFAULT);
        if (component == null || component.getAspects().isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        PlayerEntity player = client.player;

        boolean hasAspectLens = thaumaturge$hasAspectLens(player);
        boolean hasHeadGear = thaumaturge$hasAethericGogglesOrMonocle(player);

        if (hasAspectLens || hasHeadGear) {
            cir.setReturnValue(Optional.of(new AspectTooltipData(component.getMap())));
        }
    }

    @Unique
    private boolean thaumaturge$hasAspectLens(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack item = player.getInventory().getStack(i);
            if (item.getItem() == ModItems.ASPECT_LENS) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean thaumaturge$hasAethericGogglesOrMonocle(PlayerEntity player) {
        ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
        return headStack.getItem() == ModItems.AETHERIC_GOGGLES;
    }
}