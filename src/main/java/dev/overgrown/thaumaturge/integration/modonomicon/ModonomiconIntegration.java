package dev.overgrown.thaumaturge.integration.modonomicon;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class ModonomiconIntegration {
    private static final boolean MODONOMICON_LOADED = FabricLoader.getInstance().isModLoaded("modonomicon");

    public static boolean isModonomiconLoaded() {
        return MODONOMICON_LOADED;
    }

    @Nullable
    public static Object getBookGuiManager() {
        if (!MODONOMICON_LOADED) return null;

        try {
            Class<?> bookGuiManagerClass = Class.forName("com.klikli_dev.modonomicon.client.gui.BookGuiManager");
            return bookGuiManagerClass.getMethod("get").invoke(null);
        } catch (Exception e) {
            Thaumaturge.LOGGER.warn("Failed to access Modonomicon BookGuiManager", e);
            return null;
        }
    }

    public static boolean openBook(Identifier bookId) {
        if (!MODONOMICON_LOADED) return false;

        try {
            Object bookGuiManager = getBookGuiManager();
            if (bookGuiManager != null) {
                bookGuiManager.getClass().getMethod("openBook", Identifier.class).invoke(bookGuiManager, bookId);
                return true;
            }
        } catch (Exception e) {
            Thaumaturge.LOGGER.warn("Failed to open Modonomicon book: " + bookId, e);
        }
        return false;
    }

    public static void setBookOpenState(ItemStack stack, boolean open) {
        if (!MODONOMICON_LOADED) return;

        try {
            NbtCompound nbt = stack.getOrCreateNbt();
            nbt.putString("modonomicon:book_open_state", open ? "open" : "closed");

            // Also update the original open state for consistency
            nbt.putBoolean("Open", open);
        } catch (Exception e) {
            Thaumaturge.LOGGER.warn("Failed to set Modonomicon book open state", e);
        }
    }

    public static boolean isBookOpen(ItemStack stack) {
        if (!MODONOMICON_LOADED) return false;

        try {
            NbtCompound nbt = stack.getNbt();
            if (nbt != null && nbt.contains("modonomicon:book_open_state")) {
                return "open".equals(nbt.getString("modonomicon:book_open_state"));
            }
        } catch (Exception e) {
            Thaumaturge.LOGGER.warn("Failed to check Modonomicon book open state", e);
        }
        return false;
    }

    // New method to register GUI close listener
    public static void registerCloseListener() {
        if (!MODONOMICON_LOADED) return;

        try {
        } catch (Exception e) {
            Thaumaturge.LOGGER.warn("Failed to register Modonomicon close listener", e);
        }
    }
}