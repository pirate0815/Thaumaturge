package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Resolves spell casting context (aspect + modifiers) from the player's equipped focus.
 * Aspect is sourced from AspectsLib NBT:
 *   AspectsLibData -> AspectData -> aspects : { "<namespace:id>": weight, ... }
 * Picks the entry with the highest weight. Modifiers are empty for now.
 */
public final class SpellContextResolver {

    public static final class SpellContext {
        public final @Nullable Identifier aspectId;
        public final List<Identifier> modifierIds;

        public SpellContext(@Nullable Identifier aspectId, List<Identifier> modifierIds) {
            this.aspectId = aspectId;
            this.modifierIds = modifierIds;
        }
    }

    private static final String KEY_ASPECTS_LIB = "AspectsLibData";
    private static final String KEY_ASPECT_DATA  = "AspectData";
    private static final String KEY_ASPECTS      = "aspects";

    private SpellContextResolver() {}

    /** Resolve from player's main/off-hand (then hotbar) for a focus carrying AspectsLib data. */
    public static SpellContext resolve(ServerPlayerEntity player) {
        ItemStack focus = findFocusStack(player);
        Identifier aspect = readAspectIdFrom(focus);
        // TODO: resolve modifiers from your storage schema
        return new SpellContext(aspect, Collections.emptyList());
    }

    private static ItemStack findFocusStack(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        if (hasAspectsLib(main)) return main;

        ItemStack off = player.getOffHandStack();
        if (hasAspectsLib(off)) return off;

        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getStack(i);
            if (hasAspectsLib(s)) return s;
        }
        return ItemStack.EMPTY;
    }

    private static boolean hasAspectsLib(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        return tag != null && tag.contains(KEY_ASPECTS_LIB, NbtElement.COMPOUND_TYPE);
    }

    private static @Nullable Identifier readAspectIdFrom(ItemStack stack) {
        NbtCompound tag = stack.getNbt();
        if (tag == null || !tag.contains(KEY_ASPECTS_LIB, NbtElement.COMPOUND_TYPE)) return null;

        NbtCompound lib = tag.getCompound(KEY_ASPECTS_LIB);
        if (!lib.contains(KEY_ASPECT_DATA, NbtElement.COMPOUND_TYPE)) return null;

        NbtCompound data = lib.getCompound(KEY_ASPECT_DATA);
        if (!data.contains(KEY_ASPECTS, NbtElement.COMPOUND_TYPE)) return null;

        NbtCompound aspects = data.getCompound(KEY_ASPECTS);
        if (aspects.isEmpty()) return null;

        String bestKey = null;
        int bestWeight = Integer.MIN_VALUE;

        for (String key : aspects.getKeys()) {
            int weight = aspects.getInt(key); // /give example uses int weights
            if (weight > bestWeight) {
                bestWeight = weight;
                bestKey = key;
            }
        }

        if (bestKey == null) return null;

        Identifier id = Identifier.tryParse(bestKey);
        if (id == null) {
            Thaumaturge.LOGGER.warn("Invalid aspect id in AspectsLib data: {}", bestKey);
        }
        return id;
    }
}
