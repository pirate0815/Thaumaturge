package dev.overgrown.aspectslib.aspects.data;

import net.minecraft.util.Identifier;

import java.util.*;

public class EntityAspectRegistry {
    private static final Map<Identifier, AspectData> ID_TO_ASPECT = new HashMap<>();

    public static void register(Identifier entityId, AspectData data) {
        ID_TO_ASPECT.put(entityId, data);
    }

    public static AspectData get(Identifier entityId) {
        return ID_TO_ASPECT.getOrDefault(entityId, AspectData.DEFAULT);
    }

    public static void clear() {
        ID_TO_ASPECT.clear();
    }
}