package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
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
 * Backport-safe: only uses AspectEffect self/entity/aoe entry points.
 */
public final class SpellHandler {

    // radius used when a block face was targeted (no block-specific API in AspectEffect)
    private static final float BLOCK_TARGET_RADIUS = 0.75f;

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
     * Mapped to an AoE cast centered on the block to avoid adding new API to AspectEffect.
     */
    public static void castTargeted(ServerPlayerEntity player, BlockPos pos, Direction face) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        aspect.castAoe(player, pos, BLOCK_TARGET_RADIUS, mods);
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
            // ModifierRegistry#get returns a ModifierEffect (not Optional) in this backport
            ModifierEffect eff = ModifierRegistry.get(id);
            if (eff != null) {
                out.add(eff);
            }
        }
        return out;
    }
}
