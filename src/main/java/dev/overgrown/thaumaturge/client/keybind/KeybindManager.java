/**
 * KeybindManager.java
 * <p>
 * This class manages and registers all keybindings used by the Thaumaturge mod.
 * These keybindings are primarily used for casting different spells based on
 * the foci equipped in the player's gauntlet.
 * <p>
 * The mod supports up to 10 different spell keybinds (primary through denary),
 * allowing players to access different spells with different key presses.
 * <p>
 * Keybinds are registered with the Fabric API and can be reassigned in the
 * game's control settings.
 *
 * @see dev.overgrown.thaumaturge.ThaumaturgeClient
 * @see dev.overgrown.thaumaturge.spell.SpellHandler
 */
package dev.overgrown.thaumaturge.client.keybind;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeybindManager {
    // Different spell keybindings for different spell slots
    public static KeyBinding PRIMARY_SPELL; // Default: R key
    public static KeyBinding SECONDARY_SPELL; // Default: V key
    public static KeyBinding TERNARY_SPELL; // Default: G key
    public static KeyBinding QUATERNARY_SPELL; // Default: B key
    public static KeyBinding QUINARY_SPELL; // Default: H key
    public static KeyBinding SENARY_SPELL; // Default: N key
    public static KeyBinding SEPTENARY_SPELL; // Default: Y key
    public static KeyBinding OCTONARY_SPELL; // Default: U key
    public static KeyBinding NONARY_SPELL; // Default: I key
    public static KeyBinding DENARY_SPELL; // Default: O key

    /**
     * Registers all keybindings for the mod
     * Called during client initialization in ThaumaturgeClient
     */
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

    /**
     * Helper method to register a keybinding with standard naming convention
     *
     * @param name Base name for the keybind
     * @param keycode Default GLFW keycode to assign
     * @return The registered KeyBinding
     */
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