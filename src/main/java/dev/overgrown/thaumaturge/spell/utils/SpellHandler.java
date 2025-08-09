package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;          // <- corrected package
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;       // <- corrected package
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.ArrayList;
import java.util.List;

/**
 * Central entry point for executing spells on the server.
 * - Resolves Aspect + Modifiers (via SpellContextResolver and registries)
 * - Dispatches to delivery strategies (Self / Targeted / AOE)
 *
 * Notes:
 *  - If no valid Aspect is found on the player's focus, the cast is ignored.
 *  - Modifiers are resolved via ModifierRegistry using IDs from the context (may be empty).
 *  - For BLOCK-targeted casts, we provide a convenience overload without a delivery instance.
 */
public final class SpellHandler {

    private SpellHandler() {}

    // === Public API used by networking handler ===

    public static void castSelf(ServerPlayerEntity player, SelfSpellDelivery delivery) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        delivery.deliver(aspect, mods);
    }

    public static void castTargeted(ServerPlayerEntity player, TargetedSpellDelivery delivery) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        delivery.deliver(aspect, mods);
    }

    /**
     * Convenience for BLOCK-targeted casts coming from the packet (pos + face).
     */
    public static void castTargeted(ServerPlayerEntity player, BlockPos pos, Direction face) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        aspect.castOnBlock(player, pos, face, mods);
    }

    public static void castAoe(ServerPlayerEntity player, AoeSpellDelivery delivery) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        delivery.deliver(aspect, mods);
    }

    // === Helpers ===

    private static AspectEffect resolveAspect(Identifier aspectId) {
        if (aspectId == null) return null;
        return AspectRegistry.get(aspectId).orElse(null);
    }

    private static List<ModifierEffect> resolveModifiers(SpellContextResolver.SpellContext ctx) {
        if (ctx.modifiers() == null || ctx.modifiers().isEmpty()) return List.of();

        List<ModifierEffect> out = new ArrayList<>(ctx.modifiers().size());
        for (Identifier id : ctx.modifiers()) {
            ModifierRegistry.get(id).ifPresent(out::add);
        }
        return out;
    }
}
