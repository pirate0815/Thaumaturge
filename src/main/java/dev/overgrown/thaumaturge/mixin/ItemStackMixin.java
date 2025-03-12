package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.component.AspectComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
	private void addAspectTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir) {
		ItemStack stack = (ItemStack) (Object) this;
		AspectComponent component = AspectComponent.getOrDefault(stack);
		if (component != null && !component.aspects().isEmpty()) {
			cir.setReturnValue(Optional.of(new AspectTooltipData(component.aspects())));
		}
	}
}