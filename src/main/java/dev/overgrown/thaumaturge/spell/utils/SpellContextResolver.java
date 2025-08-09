package dev.overgrown.thaumaturge.spell.utils;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Resolves the active Aspect and Modifiers for a cast, using AspectsLib-style NBT on the held Focus.
 *
 * Expected NBT (example):
 *   AspectsLibData: {
 *     AspectData: {
 *       aspects: {
 *         "aspectslib:ignis": 1
 *       }
 *     }
 *   }
 *
 * This resolver is intentionally conservative: it looks at main-hand first, then off-hand.
 * Modifiers are returned empty for now (wire up when your modifier storage is finalized).
 */
public final class SpellContextResolver {

    private static final String TAG_ASPECTS_LIB = "AspectsLibData";
    private static final String TAG_ASPECT_DATA = "AspectData";
    private static final String TAG_ASPECTS     = "aspects";

    private SpellContextResolver() {}

    public static SpellContext resolve(ServerPlayerEntity player) {
        // 1) Resolve aspect id from whichever hand holds a Focus-like item with AspectsLibData
        Identifier aspectId = resolveAspectFromHands(player);

        // 2) Resolve modifiers (placeholder: empty list until storage is decided)
        List<Identifier> modifiers = List.of();

        return new SpellContext(aspectId, modifiers);
    }

    @Nullable
    private static Identifier resolveAspectFromHands(ServerPlayerEntity player) {
        ItemStack main  = player.getMainHandStack();
        ItemStack off   = player.getOffHandStack();

        Identifier id = readAspectId(main);
        if (id != null) return id;

        return readAspectId(off);
    }

    @Nullable
    private static Identifier readAspectId(ItemStack stack) {
        if (stack.isEmpty()) return null;

        NbtCompound root = stack.getNbt();
        if (root == null || !root.contains(TAG_ASPECTS_LIB, NbtCompound.COMPOUND_TYPE)) return null;

        NbtCompound lib = root.getCompound(TAG_ASPECTS_LIB);
        if (!lib.contains(TAG_ASPECT_DATA, NbtCompound.COMPOUND_TYPE)) return null;

        NbtCompound data = lib.getCompound(TAG_ASPECT_DATA);
        if (!data.contains(TAG_ASPECTS, NbtCompound.COMPOUND_TYPE)) return null;

        NbtCompound aspects = data.getCompound(TAG_ASPECTS);

        // Pick the first aspect with a positive level. Deterministic by key order.
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

    /**
     * Simple immutable context for a spell cast.
     * `aspectId` may be null if no valid aspect was found on the player.
     * `modifiers` is currently empty until modifier storage is integrated.
     */
    public record SpellContext(@Nullable Identifier aspectId, List<Identifier> modifiers) {
        public SpellContext {
            // ensure non-null list instance
            modifiers = modifiers == null ? new ArrayList<>() : List.copyOf(modifiers);
        }
    }
}
