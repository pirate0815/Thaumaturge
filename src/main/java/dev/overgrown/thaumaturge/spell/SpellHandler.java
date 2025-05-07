package dev.overgrown.thaumaturge.spell;

import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.pattern.ModifierEffect;
import dev.overgrown.thaumaturge.spell.pattern.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;

import java.util.ArrayList;
import java.util.List;

public class SpellHandler {
    public static void tryCastSpell(ServerPlayerEntity player, SpellCastPacket.SpellTier tier) {
        List<GauntletComponent.FociEntry> entries = getEquippedFociEntries(player, tier);
        if (entries.isEmpty()) return;

        Object delivery = createDelivery(tier);
        for (GauntletComponent.FociEntry entry : entries) {
            AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId());
            ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

            if (aspectEffect != null) applyEffect(delivery, aspectEffect);
            if (modifierEffect != null) applyEffect(delivery, modifierEffect);
        }

        executeDelivery(delivery, player);
    }

    private static Object createDelivery(SpellCastPacket.SpellTier tier) {
        switch (tier) {
            case LESSER: return new SelfSpellDelivery();
            case ADVANCED: return new TargetedSpellDelivery();
            case GREATER: return new AoeSpellDelivery();
            default: throw new IllegalArgumentException("Invalid tier");
        }
    }

    private static void applyEffect(Object delivery, Object effect) {
        if (delivery instanceof SelfSpellDelivery self) {
            if (effect instanceof AspectEffect ae) ae.apply(self);
            if (effect instanceof ModifierEffect me) me.apply(self);
        } else if (delivery instanceof TargetedSpellDelivery targeted) {
            if (effect instanceof AspectEffect ae) ae.apply(targeted);
            if (effect instanceof ModifierEffect me) me.apply(targeted);
        } else if (delivery instanceof AoeSpellDelivery aoe) {
            if (effect instanceof AspectEffect ae) ae.apply(aoe);
            if (effect instanceof ModifierEffect me) me.apply(aoe);
        }
    }

    private static void executeDelivery(Object delivery, ServerPlayerEntity player) {
        if (delivery instanceof SelfSpellDelivery self) self.execute(player);
        else if (delivery instanceof TargetedSpellDelivery targeted) targeted.execute(player);
        else if (delivery instanceof AoeSpellDelivery aoe) aoe.execute(player);
    }

    public static List<GauntletComponent.FociEntry> getEquippedFociEntries(ServerPlayerEntity player, SpellCastPacket.SpellTier tier) {
        List<GauntletComponent.FociEntry> entries = new ArrayList<>();
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.contains(ModComponents.MAX_FOCI)) {
                GauntletComponent component = stack.get(ModComponents.GAUNTLET_STATE);
                if (component != null) {
                    entries.addAll(component.entries().stream()
                            .filter(e -> e.tier() == tier)
                            .toList());
                }
            }
        }
        return entries;
    }

    public static SpellCastPacket.SpellTier getFociTier(Item item) {
        if (item == ModItems.LESSER_FOCI) return SpellCastPacket.SpellTier.LESSER;
        if (item == ModItems.ADVANCED_FOCI) return SpellCastPacket.SpellTier.ADVANCED;
        if (item == ModItems.GREATER_FOCI) return SpellCastPacket.SpellTier.GREATER;
        return null;
    }
}