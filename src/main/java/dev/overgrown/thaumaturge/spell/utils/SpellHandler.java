package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.pattern.SpellPattern;
import dev.overgrown.thaumaturge.spell.tier.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.*;

public final class SpellHandler {

    private SpellHandler() {}

    public static void cast(ServerPlayerEntity player, Object delivery, String keyType) {
        SpellPattern pattern = resolvePattern(player, keyType);
        if (pattern == null || pattern.getAspects().isEmpty()) return;

        // Check if this pattern contains Vinculum - if so, handle it specially
        boolean hasVinculum = pattern.getAspects().keySet().stream()
                .anyMatch(id -> id.getPath().equals("vinculum"));

        for (Map.Entry<Identifier, Identifier> entry : pattern.getAspects().entrySet()) {
            AspectEffect aspect = resolveAspect(entry.getKey());
            ModifierEffect modifier = resolveModifier(entry.getValue());
            if (aspect == null) continue;

            List<ModifierEffect> mods = modifier != null ?
                    Collections.singletonList(modifier) : Collections.emptyList();

            // If this pattern has Vinculum, only apply Vinculum and skip other aspects
            // The other aspects will be stored in the mine and triggered later
            if (hasVinculum && !entry.getKey().getPath().equals("vinculum")) {
                continue;
            }

            if (delivery instanceof SelfSpellDelivery selfDelivery) {
                selfDelivery.setModifiers(mods);
                aspect.applySelf(selfDelivery);
            }
            else if (delivery instanceof TargetedSpellDelivery targetedDelivery) {
                targetedDelivery.setModifiers(mods);
                aspect.applyTargeted(targetedDelivery);
            }
            else if (delivery instanceof AoeSpellDelivery aoeDelivery) {
                aoeDelivery.setModifiers(mods);
                aspect.applyAoe(aoeDelivery);
            }
        }
    }

    private static SpellPattern resolvePattern(ServerPlayerEntity player, String keyType) {
        ItemStack gauntlet = findGauntlet(player);
        if (gauntlet.isEmpty()) return null;

        String tier = switch (keyType) {
            case "primary" -> "lesser";
            case "secondary" -> "advanced";
            case "ternary" -> "greater";
            default -> null;
        };

        return tier != null ? SpellPattern.fromGauntlet(gauntlet, tier) : null;
    }

    private static ModifierEffect resolveModifier(Identifier modifierId) {
        if (modifierId == null) return null;
        return ModifierRegistry.get(modifierId);
    }

    public static ItemStack findGauntlet(PlayerEntity player) {
        // Check hands first
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof ResonanceGauntletItem) {
                return stack;
            }
        }

        // Check equipment slots
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.getItem() instanceof ResonanceGauntletItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    // === Helpers ===
    private static AspectEffect resolveAspect(Identifier aspectId) {
        if (aspectId == null) return null;
        return AspectRegistry.get(aspectId).orElse(null);
    }
}