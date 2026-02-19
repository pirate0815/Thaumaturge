package dev.overgrown.aspectslib.mixin.aspects.client;

import dev.overgrown.aspectslib.aspects.client.tooltip.AspectTooltipComponent;
import dev.overgrown.aspectslib.aspects.client.tooltip.AspectTooltipData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.item.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Client-side mixin for tooltip rendering (currently unused).
 * <p>
 * Note: Replaced by TooltipComponentCallback.EVENT in AspectsLibClient
 */
@Environment(EnvType.CLIENT)
@Mixin(TooltipComponent.class)
public interface TooltipComponentFactoryMixin {

    // THIS MIXIN IS NOT CALLED, IT IS REPLACED BY THE EVENT

    @Inject(method = "of(Lnet/minecraft/client/item/TooltipData;)Lnet/minecraft/client/gui/tooltip/TooltipComponent;", at = @At("HEAD"), cancellable = true)
    private static void injectAspectTooltipComponent(TooltipData data, CallbackInfoReturnable<TooltipComponent> cir) {
        if (data instanceof AspectTooltipData aspectData) {
            cir.setReturnValue(new AspectTooltipComponent(aspectData));
        }
    }
}