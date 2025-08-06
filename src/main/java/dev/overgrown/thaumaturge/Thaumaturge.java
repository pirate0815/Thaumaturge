package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thaumaturge implements ModInitializer {
    public static final String MOD_ID = "thaumaturge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier identifier(String path) {
        return new Identifier(Thaumaturge.MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Register all items
        ModItems.initialize();

        LOGGER.info("Thaumaturge initialized!");
    }
}