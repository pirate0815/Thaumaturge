package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import dev.overgrown.thaumaturge.spell.effect.SpellEffect;
import dev.overgrown.thaumaturge.spell.tier.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellHandler {
    public static void castSpell(PlayerEntity player, Hand hand, int spellKey) {
        ItemStack stack = player.getStackInHand(hand);
        if (!(stack.getItem() instanceof ResonanceGauntletItem gauntlet)) return;

        NbtList fociNbt = gauntlet.getFoci(stack);
        List<SpellFocus> foci = new ArrayList<>();

        for (NbtElement element : fociNbt) {
            NbtCompound focusNbt = (NbtCompound) element;
            ItemStack focusStack = ItemStack.fromNbt(focusNbt);
            if (focusStack.getItem() instanceof FocusItem focusItem) {
                Identifier aspectId = focusItem.getAspect(focusStack);
                Identifier modifierId = focusItem.getModifier(focusStack);

                // Use raw Identifier instead of string
                foci.add(new SpellFocus(
                        focusItem.getTier(),
                        aspectId,
                        modifierId
                ));
            }
        }

        Map<String, List<SpellFocus>> grouped = groupFociByTier(foci);
        SpellDelivery delivery = null;
        List<SpellEffect> effects = new ArrayList<>();

        switch (spellKey) {
            case 0 -> delivery = new SelfSpellDelivery();
            case 1 -> delivery = new TargetedSpellDelivery();
            case 2 -> delivery = new AoeSpellDelivery();
        }

        // Get the tier for this spell key
        String tier = tierForSpellKey(spellKey);
        if (tier != null) {
            List<SpellFocus> tierFoci = grouped.getOrDefault(tier, new ArrayList<>());
            effects = combineEffects(tierFoci);
        }

        if (delivery != null && !effects.isEmpty()) {
            delivery.cast(player.getWorld(), player, effects);
        }
    }

    private static String tierForSpellKey(int key) {
        return switch (key) {
            case 0 -> "lesser";
            case 1 -> "advanced";
            case 2 -> "greater";
            default -> null;
        };
    }

    private static List<SpellEffect> combineEffects(List<SpellFocus> foci) {
        Map<Identifier, SpellEffect> effectMap = new HashMap<>();

        for (SpellFocus focus : foci) {
            Identifier aspectId = focus.aspect();
            Identifier modifierId = focus.modifier();

            // Create or update existing effect
            SpellEffect effect = effectMap.get(aspectId);
            if (effect == null) {
                effect = new SpellEffect(aspectId, modifierId, 1);
                effectMap.put(aspectId, effect);
            } else {
                // Increase amplification for same aspect
                effect = new SpellEffect(
                        effect.aspectId(),
                        effect.modifierId(),
                        effect.amplifier() + 1
                );
                effectMap.put(aspectId, effect);
            }
        }

        return new ArrayList<>(effectMap.values());
    }

    private static Map<String, List<SpellFocus>> groupFociByTier(List<SpellFocus> foci) {
        Map<String, List<SpellFocus>> grouped = new HashMap<>();
        grouped.put("lesser", new ArrayList<>());
        grouped.put("advanced", new ArrayList<>());
        grouped.put("greater", new ArrayList<>());

        for (SpellFocus focus : foci) {
            switch (focus.tier()) {
                case "lesser" -> grouped.get("lesser").add(focus);
                case "advanced" -> grouped.get("advanced").add(focus);
                case "greater" -> grouped.get("greater").add(focus);
            }
        }
        return grouped;
    }

    private record SpellFocus(String tier, Identifier aspect, Identifier modifier) {}
}