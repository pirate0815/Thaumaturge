package dev.overgrown.thaumaturge.spell.pattern;

import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import java.util.*;

public class SpellPattern {
    private final String tier;
    private final Map<Identifier, Identifier> aspects; // Aspect ID -> Modifier ID
    private final int amplifier;

    public SpellPattern(String tier, Map<Identifier, Identifier> aspects, int amplifier) {
        this.tier = tier;
        this.aspects = aspects;
        this.amplifier = amplifier;
    }

    public static SpellPattern fromGauntlet(ItemStack gauntlet, String tier) {
        if (!(gauntlet.getItem() instanceof ResonanceGauntletItem)) return null;

        ResonanceGauntletItem gauntletItem = (ResonanceGauntletItem) gauntlet.getItem();
        NbtList foci = gauntletItem.getFoci(gauntlet);
        Map<Identifier, Identifier> aspects = new LinkedHashMap<>();

        for (NbtElement element : foci) {
            ItemStack focusStack = ItemStack.fromNbt((NbtCompound) element);
            if (!(focusStack.getItem() instanceof FocusItem)) continue;

            FocusItem focus = (FocusItem) focusStack.getItem();
            if (!focus.getTier().equals(tier)) continue;

            Identifier aspect = focus.getAspect(focusStack);
            Identifier modifier = focus.getModifier(focusStack);
            aspects.put(aspect, modifier);
        }

        return aspects.isEmpty() ? null : new SpellPattern(tier, aspects, 1);
    }

    public String getTier() { return tier; }
    public Map<Identifier, Identifier> getAspects() { return aspects; }
    public int getAmplifier() { return amplifier; }

    public boolean matchesTier(String keyType) {
        return switch (keyType) {
            case "primary" -> "lesser".equals(tier);
            case "secondary" -> "advanced".equals(tier);
            case "ternary" -> "greater".equals(tier);
            default -> false;
        };
    }
}