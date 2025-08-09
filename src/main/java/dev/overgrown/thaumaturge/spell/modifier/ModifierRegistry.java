package dev.overgrown.thaumaturge.spell.modifier;

import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

public class ModifierRegistry {
    private static final Map<Identifier, ModifierEffect> REGISTRY = new HashMap<>();

    public static void register(Identifier modifierId, ModifierEffect effect) {
        REGISTRY.put(modifierId, effect);
    }

    public static ModifierEffect get(Identifier modifierId) {
        return REGISTRY.get(modifierId);
    }
}