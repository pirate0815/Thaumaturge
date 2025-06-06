package dev.overgrown.thaumaturge.compat.modmenu.config;

import com.google.gson.*;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public class AspectConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("thaumaturge.json");
    public static boolean ALWAYS_SHOW_ASPECTS = false;

    static {
        load();
    }

    public static void load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return;
        }

        try (BufferedReader reader = Files.newBufferedReader(CONFIG_PATH)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("alwaysShowAspects")) {
                ALWAYS_SHOW_ASPECTS = json.get("alwaysShowAspects").getAsBoolean();
            }
        } catch (Exception e) {
            Thaumaturge.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        JsonObject json = new JsonObject();
        json.addProperty("alwaysShowAspects", ALWAYS_SHOW_ASPECTS);

        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_PATH)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
        } catch (Exception e) {
            Thaumaturge.LOGGER.error("Failed to save config", e);
        }
    }
}