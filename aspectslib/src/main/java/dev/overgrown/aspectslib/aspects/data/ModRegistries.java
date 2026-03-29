package dev.overgrown.aspectslib.aspects.data;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for mod data.
 * <p>
 * Responsibilities:
 * <ol type="1">
 * <li>Stores all loaded aspects</li>
 * </ol>
 * </p>
 * <p>
 * Usage:
 * <li>Accessed throughout the mod for aspect lookups</li>
 * <li>Populated by {@link AspectManager}</li>
 * </p>
 */
public class ModRegistries {
    public static final Map<Identifier, Aspect> ASPECTS = new HashMap<>();
}