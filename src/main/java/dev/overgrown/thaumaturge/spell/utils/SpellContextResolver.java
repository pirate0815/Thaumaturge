package dev.overgrown.thaumaturge.spell.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Resolves the active Aspect and Modifiers for a cast.
 * Backport note: also scans the held item's NBT recursively so a Focus stored
 * inside a Gauntlet (or similar container) is detected.
 */
public final class SpellContextResolver {

    private static final String TAG_ASPECTS_LIB = "AspectsLibData";
    private static final String TAG_ASPECT_DATA = "AspectData";
    private static final String TAG_ASPECTS     = "aspects";

    private SpellContextResolver() {}

    public static SpellContext resolve(ServerPlayerEntity player) {
        Identifier aspectId = resolveAspectFromEquipment(player);
        List<Identifier> modifiers = List.of(); // wire when modifier storage is finalized
        return new SpellContext(aspectId, modifiers);
    }

    @Nullable
    private static Identifier resolveAspectFromEquipment(ServerPlayerEntity player) {
        // Prefer main hand, then offhand — each may be a Focus OR a Gauntlet holding a Focus.
        Identifier id = readAspectId(player.getMainHandStack());
        if (id != null) return id;
        return readAspectId(player.getOffHandStack());
    }

    /**
     * Try to read AspectsLib data directly from the stack; if not present,
     * recursively search all nested compounds/lists for AspectsLibData.
     */
    @Nullable
    private static Identifier readAspectId(ItemStack stack) {
        if (stack.isEmpty()) return null;

        NbtCompound root = stack.getNbt();
        if (root == null) return null;

        // 1) Direct path: AspectsLibData -> AspectData -> aspects
        Identifier direct = readAspectIdDirect(root);
        if (direct != null) return direct;

        // 2) Deep path: search recursively for any nested AspectsLibData blob
        return findAspectIdDeep(root);
    }

    @Nullable
    private static Identifier readAspectIdDirect(NbtCompound compound) {
        if (!compound.contains(TAG_ASPECTS_LIB, NbtCompound.COMPOUND_TYPE)) return null;
        NbtCompound lib = compound.getCompound(TAG_ASPECTS_LIB);
        if (!lib.contains(TAG_ASPECT_DATA, NbtCompound.COMPOUND_TYPE)) return null;
        NbtCompound data = lib.getCompound(TAG_ASPECT_DATA);
        if (!data.contains(TAG_ASPECTS, NbtCompound.COMPOUND_TYPE)) return null;

        NbtCompound aspects = data.getCompound(TAG_ASPECTS);
        Set<String> keys = aspects.getKeys();
        for (String key : keys) {
            int level = aspects.getInt(key);
            if (level > 0) {
                try {
                    return new Identifier(key);
                } catch (IllegalArgumentException ignored) {
                    // bad id string; skip
                }
            }
        }
        return null;
    }

    @Nullable
    private static Identifier findAspectIdDeep(NbtElement element) {
        if (element == null) return null;

        if (element instanceof NbtCompound compound) {
            // Check direct structure at this node
            Identifier id = readAspectIdDirect(compound);
            if (id != null) return id;

            // Recurse into children
            for (String k : compound.getKeys()) {
                NbtElement child = compound.get(k);
                Identifier nested = findAspectIdDeep(child);
                if (nested != null) return nested;
            }
        } else if (element instanceof NbtList list) {
            for (int i = 0; i < list.size(); i++) {
                Identifier nested = findAspectIdDeep(list.get(i));
                if (nested != null) return nested;
            }
        }
        return null;
    }

    /**
     * Simple immutable context for a spell cast.
     * `aspectId` may be null if no valid aspect was found on the player.
     * `modifiers` is currently empty until modifier storage is integrated.
     */
    public record SpellContext(@Nullable Identifier aspectId, List<Identifier> modifiers) {
        public SpellContext {
            modifiers = modifiers == null ? new ArrayList<>() : List.copyOf(modifiers);
        }
    }
}
