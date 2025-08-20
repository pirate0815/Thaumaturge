package dev.overgrown.thaumaturge.client.keybind;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * KeybindManager.java
 * Registers up to 10 spell keybinds (primary → denary).
 * No tick handling here—ThaumaturgeClient handles presses.
 */
public class KeybindManager {
    public static KeyBinding PRIMARY_SPELL;    // R
    public static KeyBinding SECONDARY_SPELL;  // V
    public static KeyBinding TERNARY_SPELL;    // G
    public static KeyBinding QUATERNARY_SPELL; // B
    public static KeyBinding QUINARY_SPELL;    // H
    public static KeyBinding SENARY_SPELL;     // N
    public static KeyBinding SEPTENARY_SPELL;  // Y
    public static KeyBinding OCTONARY_SPELL;   // U
    public static KeyBinding NONARY_SPELL;     // I
    public static KeyBinding DENARY_SPELL;     // O

    /** Called during client init from ThaumaturgeClient. */
    public static void registerKeybinds() {
        PRIMARY_SPELL    = registerKey("primary",    GLFW.GLFW_KEY_R);
        SECONDARY_SPELL  = registerKey("secondary",  GLFW.GLFW_KEY_V);
        TERNARY_SPELL    = registerKey("ternary",    GLFW.GLFW_KEY_G);
        QUATERNARY_SPELL = registerKey("quaternary", GLFW.GLFW_KEY_B);
        QUINARY_SPELL    = registerKey("quinary",    GLFW.GLFW_KEY_H);
        SENARY_SPELL     = registerKey("senary",     GLFW.GLFW_KEY_N);
        SEPTENARY_SPELL  = registerKey("septenary",  GLFW.GLFW_KEY_Y);
        OCTONARY_SPELL   = registerKey("octonary",   GLFW.GLFW_KEY_U);
        NONARY_SPELL     = registerKey("nonary",     GLFW.GLFW_KEY_I);
        DENARY_SPELL     = registerKey("denary",     GLFW.GLFW_KEY_O);
    }

    private static KeyBinding registerKey(String name, int keycode) {
        String translationKey = "key." + Thaumaturge.MOD_ID + "." + name;
        String categoryKey    = "key.category." + Thaumaturge.MOD_ID + ".spells";
        return KeyBindingHelper.registerKeyBinding(
                new KeyBinding(translationKey, InputUtil.Type.KEYSYM, keycode, categoryKey)
        );
    }
}
