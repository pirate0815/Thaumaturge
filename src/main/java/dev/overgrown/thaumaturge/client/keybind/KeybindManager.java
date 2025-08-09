package dev.overgrown.thaumaturge.client.keybind;

import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Registers spell keybinds and forwards presses to networking senders.
 * Default keys:
 *  - Z: Self cast
 *  - X: Targeted cast (block/entity under crosshair)
 *  - C: AOE cast (centered at crosshair block or player) with fixed radius
 */
public final class KeybindManager implements ClientModInitializer {
    private static KeyBinding CAST_SELF;
    private static KeyBinding CAST_TARGETED;
    private static KeyBinding CAST_AOE;

    // Default AOE radius; server clamps anyway
    private static final float DEFAULT_AOE_RADIUS = 3.0f;

    @Override
    public void onInitializeClient() {
        CAST_SELF = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.thaumaturge.cast_self",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category.thaumaturge.spells"
        ));
        CAST_TARGETED = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.thaumaturge.cast_targeted",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "category.thaumaturge.spells"
        ));
        CAST_AOE = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.thaumaturge.cast_aoe",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.thaumaturge.spells"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (CAST_SELF.wasPressed()) {
                SpellCastPacket.sendSelf();
            }
            while (CAST_TARGETED.wasPressed()) {
                SpellCastPacket.sendTargetedFromCrosshair();
            }
            while (CAST_AOE.wasPressed()) {
                SpellCastPacket.sendAoeFromCrosshair(DEFAULT_AOE_RADIUS);
            }
        });
    }
}
