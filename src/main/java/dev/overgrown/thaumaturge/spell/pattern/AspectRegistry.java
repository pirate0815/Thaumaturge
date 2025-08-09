package dev.overgrown.thaumaturge.spell.pattern;

import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

public class AspectRegistry {
    private static final Map<Identifier, AspectEffect> REGISTRY = new HashMap<>();

    public static void register(Identifier id, AspectEffect effect) {
        REGISTRY.put(id, effect);
    }

    public static AspectEffect get(Identifier id) {
        return REGISTRY.get(id);
    }
}