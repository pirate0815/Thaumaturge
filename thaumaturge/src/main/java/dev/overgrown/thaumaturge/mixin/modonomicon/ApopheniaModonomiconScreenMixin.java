package dev.overgrown.thaumaturge.mixin.modonomicon;

import dev.overgrown.thaumaturge.item.apophenia.ApopheniaItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ApopheniaModonomiconScreenMixin {

    @Inject(method = "close", at = @At("HEAD"))
    private void onScreenClose(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        // Check if this screen is a Modonomicon book screen
        if (self.getClass().getName().startsWith("com.klikli_dev.modonomicon.client.gui.book")) {
            // Update any Apophenia items that might be open
            if (client.player != null) {
                for (Hand hand : Hand.values()) {
                    ItemStack stack = client.player.getStackInHand(hand);
                    if (stack.getItem() instanceof ApopheniaItem) {
                        ApopheniaItem.onBookClosed(stack);
                    }
                }
            }
        }
    }
}