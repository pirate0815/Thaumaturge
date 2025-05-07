package dev.overgrown.thaumaturge.spell.pattern;

import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public class AspectRegistry {
    private static final Map<Identifier, AspectEffect> REGISTRY = new HashMap<>();

    public static void register(Identifier aspectId, AspectEffect effect) {
        REGISTRY.put(aspectId, effect);
    }

    public static AspectEffect get(Identifier aspectId) {
        return REGISTRY.get(aspectId);
    }
}