package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import dev.overgrown.thaumaturge.networking.GauntletCastPackets;
import dev.overgrown.thaumaturge.spell.input.GauntletInput;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Client-side combo detection for gauntlet casting.
 *
 * <p>Intercepts raw left/right clicks in {@code handleInputEvents} when the player
 * holds a gauntlet, building a 3-input pattern. When the pattern reaches 3 inputs,
 * the corresponding slot index is sent to the server via {@link GauntletCastPackets}.
 *
 * <p>This is basically copying how the Arcanus: Continuum mod handles spell combos,
 * each physical click registers exactly one input, avoiding the double-registration
 * problems of server-side packet interception.
 */
@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public abstract class GauntletComboMixin {

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Final
    public GameOptions options;

    @Unique
    private final List<GauntletInput> thaumaturge$pattern = new ArrayList<>(3);

    @Unique
    private int thaumaturge$comboTimer = 0;

    /** Timer tick */
    @Inject(
            method = "tick",
            at = @At(
                    "HEAD"
            )
    )
    private void thaumaturge$onTick(CallbackInfo ci) {
        if (player == null) return;

        if (thaumaturge$comboTimer > 0) {
            thaumaturge$comboTimer--;
        }

        // Clear stale pattern when timer expires
        if (thaumaturge$comboTimer == 0 && !thaumaturge$pattern.isEmpty()) {
            thaumaturge$pattern.clear();
        }
    }

    /** Left-click -> LEFT input */
    @Inject(
            method = "handleInputEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;doAttack()Z",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void thaumaturge$onLeftClick(CallbackInfo ci) {
        if (!thaumaturge$isHoldingGauntlet()) return;

        thaumaturge$addInput(GauntletInput.LEFT);
        ci.cancel();
    }

    /** Right-click → RIGHT input */
    @Inject(
            method = "handleInputEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;doItemUse()V",
                    ordinal = 0
            ),
            cancellable = true
    )
    private void thaumaturge$onRightClick(CallbackInfo ci) {
        if (!thaumaturge$isHoldingGauntlet()) return;

        // Allow sneak+right-click to pass through for faucet management
        if (player.isSneaking()) return;

        thaumaturge$addInput(GauntletInput.RIGHT);
        ci.cancel();
    }

    /** Cancel continuous mining/use while building a combo */
    @Inject(
            method = "handleInputEvents",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/MinecraftClient;handleBlockBreaking(Z)V"
            ),
            cancellable = true
    )
    private void thaumaturge$onContinueAttack(CallbackInfo ci) {
        if (thaumaturge$isHoldingGauntlet() && !thaumaturge$pattern.isEmpty()) {
            ci.cancel();
        }
    }

    /** Helpers: */
    @Unique
    private boolean thaumaturge$isHoldingGauntlet() {
        if (player == null || player.isSpectator()) return false;
        return player.getMainHandStack().getItem() instanceof ResonanceGauntletItem;
    }

    @Unique
    private void thaumaturge$addInput(GauntletInput input) {
        thaumaturge$comboTimer = 20; // 1-second window
        thaumaturge$pattern.add(input);

        // Visual + audio feedback
        player.swingHand(Hand.MAIN_HAND);
        player.getWorld().playSound(player, player.getX(), player.getY(), player.getZ(),
                SoundEvents.UI_BUTTON_CLICK.value(), SoundCategory.PLAYERS, 0.5f,
                input == GauntletInput.RIGHT ? 1.1f : 1.3f);

        // When 3 inputs collected, resolve and send
        if (thaumaturge$pattern.size() >= 3) {
            int slotIndex = thaumaturge$resolveSlot();
            if (slotIndex >= 0) {
                ItemStack stack = player.getMainHandStack();
                ResonanceGauntletItem gauntlet = (ResonanceGauntletItem) stack.getItem();
                if (slotIndex < gauntlet.getFocusSlots()) {
                    GauntletCastPackets.sendCast(slotIndex);
                }
            }
            thaumaturge$pattern.clear();
            thaumaturge$comboTimer = 0;
        }
    }

    /**
     * Maps a 3-input pattern to a focus slot index.
     * R-L-L -> 0,  L-R-L -> 1,  R-L-R -> 2
     */
    @Unique
    private int thaumaturge$resolveSlot() {
        if (thaumaturge$pattern.size() < 3) return -1;
        String p = thaumaturge$pattern.get(0).toString()
                + thaumaturge$pattern.get(1).toString()
                + thaumaturge$pattern.get(2).toString();
        return switch (p) {
            case "RLL" -> 0;
            case "LRL" -> 1;
            case "RLR" -> 2;
            default -> -1; // Invalid combo
        };
    }
}
