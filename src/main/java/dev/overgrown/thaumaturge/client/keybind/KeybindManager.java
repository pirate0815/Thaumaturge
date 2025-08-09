package dev.overgrown.thaumaturge.client.keybind;

import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public final class KeybindManager implements ClientModInitializer {

    private static KeyBinding SELF_CAST;
    private static KeyBinding TARGETED_CAST;
    private static KeyBinding AOE_CAST;

    // default AOE radius (server clamps anyway)
    private static final float DEFAULT_AOE_RADIUS = 3.0f;

    @Override
    public void onInitializeClient() {
        SELF_CAST = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.thaumaturge.cast_self",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "key.categories.thaumaturge"
        ));

        TARGETED_CAST = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.thaumaturge.cast_targeted",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                "key.categories.thaumaturge"
        ));

        AOE_CAST = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.thaumaturge.cast_aoe",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "key.categories.thaumaturge"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            while (SELF_CAST.wasPressed()) {
                SpellCastPacket.sendSelf();
            }
            while (TARGETED_CAST.wasPressed()) {
                SpellCastPacket.sendTargetedFromCrosshair();
            }
            while (AOE_CAST.wasPressed()) {
                SpellCastPacket.sendAoeFromCrosshair(DEFAULT_AOE_RADIUS);
            }
        });
    }
}
