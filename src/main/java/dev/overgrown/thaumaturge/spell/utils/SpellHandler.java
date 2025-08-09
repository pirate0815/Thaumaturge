package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Central spell dispatch. Treats client packets as "intent", resolves the Aspect and Modifiers
 * from the player's equipped focus (AspectsLib), builds the appropriate delivery, and lets
 * the Aspect perform the effect.
 *
 * NOTE:
 *  - Delivery types are data-holders for targets (self / block+face / entity / aoe).
 *  - AspectRegistry maps aspect IDs to AspectEffect implementations.
 *  - ModifierRegistry maps modifier IDs to ModifierEffect implementations.
 */
public final class SpellHandler {
    private SpellHandler() {}

    /* ===================== ENTRY POINTS USED BY NETWORK HANDLER ===================== */

    public static void castSelf(ServerPlayerEntity player, SelfSpellDelivery ignored) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId);
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx.modifierIds);

        // Rebuild delivery with resolved context
        SelfSpellDelivery delivery = new SelfSpellDelivery(player, aspect, mods);
        try {
            aspect.applySelf(delivery);
        } catch (Throwable t) {
            Thaumaturge.LOGGER.error("Spell self-cast failed for aspect {}", ctx.aspectId, t);
        }
    }

    /** Targeted cast on an entity. */
    public static void castTargeted(ServerPlayerEntity player, TargetedSpellDelivery deliveryWithTargetEntity) {
        // deliveryWithTargetEntity is expected to carry either an entity OR a (pos, face).
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId);
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx.modifierIds);

        TargetedSpellDelivery delivery = deliveryWithTargetEntity.withContext(aspect, mods);
        try {
            aspect.applyTargeted(delivery);
        } catch (Throwable t) {
            Thaumaturge.LOGGER.error("Spell targeted-cast failed for aspect {}", ctx.aspectId, t);
        }
    }

    /** Targeted cast on a block face. Convenience for network path that passes pos/face. */
    public static void castTargeted(ServerPlayerEntity player, BlockPos pos, Direction face) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId);
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx.modifierIds);

        TargetedSpellDelivery delivery = new TargetedSpellDelivery(player, aspect, mods, pos, face);
        try {
            aspect.applyTargeted(delivery);
        } catch (Throwable t) {
            Thaumaturge.LOGGER.error("Spell targeted-cast (block) failed for aspect {}", ctx.aspectId, t);
        }
    }

    public static void castAoe(ServerPlayerEntity player, AoeSpellDelivery ignored) {
        // ignored contains center+radius already; we rebuild with aspect/mods.
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId);
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx.modifierIds);

        AoeSpellDelivery delivery = ignored.withContext(aspect, mods);
        try {
            aspect.applyAoe(delivery);
        } catch (Throwable t) {
            Thaumaturge.LOGGER.error("Spell AOE-cast failed for aspect {}", ctx.aspectId, t);
        }
    }

    /** Convenience overload if you want to call with raw center+radius. */
    public static void castAoe(ServerPlayerEntity player, BlockPos center, float radius) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId);
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx.modifierIds);

        AoeSpellDelivery delivery = new AoeSpellDelivery(player, aspect, mods, center, radius);
        try {
            aspect.applyAoe(delivery);
        } catch (Throwable t) {
            Thaumaturge.LOGGER.error("Spell AOE-cast failed for aspect {}", ctx.aspectId, t);
        }
    }

    /* ===================== HELPERS ===================== */

    private static AspectEffect resolveAspect(Identifier id) {
        if (id == null) return null;
        AspectEffect effect = AspectRegistry.get(id);
        if (effect == null) {
            Thaumaturge.LOGGER.warn("No AspectEffect registered for id {}", id);
        }
        return effect;
    }

    private static List<ModifierEffect> resolveModifiers(List<Identifier> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        List<ModifierEffect> out = new ArrayList<>(ids.size());
        for (Identifier id : ids) {
            ModifierEffect eff = ModifierRegistry.get(id);
            if (eff != null) out.add(eff);
        }
        return out;
    }
}
