package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
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
 * Central entry for executing spells on the server using delivery-based Aspect API.
 * Parity with original: AspectEffect.apply*(delivery) drives behavior.
 */
public final class SpellHandler {

    private SpellHandler() {}

    // === Public API used by networking handler ===

    public static void castSelf(ServerPlayerEntity player, SelfSpellDelivery delivery) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        delivery.setModifiers(mods);
        aspect.applySelf(delivery);
    }

    public static void castTargeted(ServerPlayerEntity player, TargetedSpellDelivery delivery) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        delivery.setModifiers(mods);
        aspect.applyTargeted(delivery);
    }

    /** Convenience for BLOCK-target casts coming from networking (pos + face). */
    public static void castTargeted(ServerPlayerEntity player, BlockPos pos, Direction face) {
        TargetedSpellDelivery delivery = new TargetedSpellDelivery(player, pos, face);

        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        delivery.setModifiers(mods);
        aspect.applyTargeted(delivery);
    }

    public static void castAoe(ServerPlayerEntity player, AoeSpellDelivery delivery) {
        SpellContextResolver.SpellContext ctx = SpellContextResolver.resolve(player);
        AspectEffect aspect = resolveAspect(ctx.aspectId());
        if (aspect == null) return;

        List<ModifierEffect> mods = resolveModifiers(ctx);
        delivery.setModifiers(mods);
        aspect.applyAoe(delivery);
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
            ModifierEffect eff = ModifierRegistry.get(id);
            if (eff != null) out.add(eff);
        }
        return out;
    }
}
