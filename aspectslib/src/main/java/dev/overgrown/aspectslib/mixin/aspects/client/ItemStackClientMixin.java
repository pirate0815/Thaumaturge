package dev.overgrown.aspectslib.mixin.aspects.client;

import dev.overgrown.aspectslib.aspects.api.IAspectDataProvider;
import dev.overgrown.aspectslib.aspects.client.AspectsTooltipConfig;
import dev.overgrown.aspectslib.aspects.client.tooltip.AspectTooltipComponent;
import dev.overgrown.aspectslib.aspects.client.tooltip.AspectTooltipData;
import dev.overgrown.aspectslib.aspects.data.AspectData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * Client-side mixin to provide aspect tooltip data.
 * <p>
 * Responsibilities:
 * <ol type="1">
 * <li>Attach AspectTooltipData to items with aspects</li>
 * </ol>
 * <p>
 * Important Connections:
 * <li>{@link IAspectDataProvider}: Source of aspect data</li>
 * <li>{@link AspectTooltipComponent}: Renders the tooltip</li>
 */
@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {

    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void addAspectTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        IAspectDataProvider provider = (IAspectDataProvider) this;
        AspectData aspectData = provider.aspectslib$getAspectData();

        if (aspectData == null || aspectData.isEmpty()) {
            return;
        }

        // Check if we should show aspects based on conditions
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (!AspectsTooltipConfig.shouldShowTooltip((ItemStack)(Object)this, player)) {
            return;
        }

        cir.setReturnValue(Optional.of(new AspectTooltipData(aspectData)));
    }
}