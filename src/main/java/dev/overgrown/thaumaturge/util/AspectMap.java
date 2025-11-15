package dev.overgrown.thaumaturge.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AspectMap {

    private HashMap<Identifier, Integer> map;
    private int count;

    public AspectMap() {
        map = new HashMap<>();
        count = 0;
    }

    public int getTotalAspectLevel() {
        return count;
    }

    public @Unmodifiable Set<Identifier> getAspects() {
        return Collections.unmodifiableSet(map.keySet());
    }

    public int getAspectLevel(@NotNull Identifier identifier) {
        return map.getOrDefault(identifier, 0);
    }

    public void setAspectLevel(@NotNull Identifier identifier, int level) {
        int previousLevel = getAspectLevel(identifier);
        if (level <= 0) {
            count = count - previousLevel;
            map.remove(identifier);
        } else {
            count = count + (level - previousLevel);
            map.put(identifier, level);
        }
    }

    public void modifyAspectLevel(@NotNull Identifier identifier, int levelDelta) {
        int previousLevel = getAspectLevel(identifier);
        int newLevel = previousLevel + levelDelta;
        if (newLevel > 0) {
            count = count + levelDelta;
            map.put(identifier, newLevel);
        } else if (map.containsKey(identifier)){
            count = count - previousLevel;
            map.remove(identifier);
        }
    }

    public NbtCompound toCompound() {
        NbtCompound nbt = new NbtCompound();
        for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {
            nbt.putInt(entry.getKey().toString(), entry.getValue());
        }
        return nbt;
    }

    public void fromNbt(NbtCompound nbt) {
        map.clear();
        for (String key : nbt.getKeys()) {
            Identifier identifier = Identifier.tryParse(key);
            if (identifier != null) {
                map.put(identifier, nbt.getInt(key));
            }
        }
        count = map.values().stream().mapToInt(Integer::intValue).sum();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public void clear() {
        this.map.clear();
        this.count = 0;
    }
}
