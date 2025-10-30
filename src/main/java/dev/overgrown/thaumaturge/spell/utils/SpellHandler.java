package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.aspectslib.aether.AetherAPI;
import dev.overgrown.aspectslib.data.AspectData;
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
import net.minecraft.util.math.BlockPos;

import java.util.*;

public final class SpellHandler {

    private SpellHandler() {}

    public static boolean cast(ServerPlayerEntity player, Object delivery, String keyType) {
        SpellPattern pattern = resolvePattern(player, keyType);
        if (pattern == null || pattern.getAspects().isEmpty()) return false;

        // Calculate spell cost from aspects
        AspectData spellCost = calculateSpellCost(pattern);

        // Check if we can cast the spell (enough Aether)
        BlockPos castPos = player.getBlockPos();
        if (!AetherAPI.canCastSpell(player.getWorld(), castPos, spellCost)) {
            // Failed cast - chance to create dead zone
            handleFailedCast(player, castPos, spellCost);
            return false;
        }

        // Consume Aether for the spell
        if (!AetherAPI.castSpell(player.getWorld(), castPos, spellCost)) {
            // This shouldn't happen if canCastSpell passed, but handle it anyway
            handleFailedCast(player, castPos, spellCost);
            return false;
        }

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

        return true;
    }

    private static AspectData calculateSpellCost(SpellPattern pattern) {
        // Create aspect data with base costs for each aspect in the pattern
        AspectData.Builder costBuilder = new AspectData.Builder(AspectData.DEFAULT);

        for (Identifier aspectId : pattern.getAspects().keySet()) {
            // Base cost of 1 RU per aspect, multiplied by amplifier
            int baseCost = 1 * pattern.getAmplifier();
            costBuilder.add(aspectId, baseCost);
        }

        return costBuilder.build();
    }

    private static void handleFailedCast(ServerPlayerEntity player, BlockPos castPos, AspectData attemptedCost) {
        // Calculate total attempted cost for dead zone chance calculation
        double totalCost = attemptedCost.calculateTotalRU();

        // Chance to create dead zone based on how much Aether was missing
        // Higher cost spells have higher chance
        double deadZoneChance = Math.min(0.3, totalCost * 0.05); // Max 30% chance

        if (player.getWorld().random.nextDouble() < deadZoneChance) {
            // Create temporary dead zone (80% temporary, 20% permanent for very high cost spells)
            boolean permanent = totalCost > 10 && player.getWorld().random.nextDouble() < 0.2;

            if (permanent) {
                AetherAPI.createPermanentDeadZone(player.getWorld(), castPos);
            } else {
                AetherAPI.createTemporaryDeadZone(player.getWorld(), castPos);
            }

            // Notify player
            player.sendMessage(net.minecraft.text.Text.literal("§cThe spell backfires! A dead zone forms."), false);
        } else {
            player.sendMessage(net.minecraft.text.Text.literal("§cNot enough Aether to cast this spell."), false);
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