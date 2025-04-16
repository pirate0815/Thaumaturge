package dev.overgrown.thaumaturge.client.keybind;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    public static KeyBinding PRIMARY_SPELL;
    public static KeyBinding SECONDARY_SPELL;
    public static KeyBinding TERNARY_SPELL;
    public static KeyBinding QUATERNARY_SPELL;
    public static KeyBinding QUINARY_SPELL;
    public static KeyBinding SENARY_SPELL;
    public static KeyBinding SEPTENARY_SPELL;
    public static KeyBinding OCTONARY_SPELL;
    public static KeyBinding NONARY_SPELL;
    public static KeyBinding DENARY_SPELL;

    public static void registerKeybinds() {
        PRIMARY_SPELL = registerKey("primary", GLFW.GLFW_KEY_R);
        SECONDARY_SPELL = registerKey("secondary", GLFW.GLFW_KEY_V);
        TERNARY_SPELL = registerKey("ternary", GLFW.GLFW_KEY_G);
        QUATERNARY_SPELL = registerKey("quaternary", GLFW.GLFW_KEY_B);
        QUINARY_SPELL = registerKey("quinary", GLFW.GLFW_KEY_H);
        SENARY_SPELL = registerKey("senary", GLFW.GLFW_KEY_N);
        SEPTENARY_SPELL = registerKey("septenary", GLFW.GLFW_KEY_Y);
        OCTONARY_SPELL = registerKey("octonary", GLFW.GLFW_KEY_U);
        NONARY_SPELL = registerKey("nonary", GLFW.GLFW_KEY_I);
        DENARY_SPELL = registerKey("denary", GLFW.GLFW_KEY_O);
    }

    private static KeyBinding registerKey(String name, int keycode) {
        String translationKey = "key." + Thaumaturge.MOD_ID + "." + name;
        String categoryKey = "key.category." + Thaumaturge.MOD_ID + ".spells";

        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding(
                        translationKey,
                        InputUtil.Type.KEYSYM,
                        keycode,
                        categoryKey
                )
        );
    }
}