package dev.overgrown.aspectslib.spell.conduit;

import dev.overgrown.aspectslib.spell.SpellContext;
import dev.overgrown.aspectslib.spell.modifier.ModifierRegistry;
import dev.overgrown.aspectslib.spell.modifier.SpellModifier;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConduitItem extends Item implements IConduit {

    private static final String KEY_SPELL_ID  = "SpellId";
    private static final String KEY_MODIFIERS = "Modifiers";

    public ConduitItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean canCast(ItemStack stack, LivingEntity caster) {
        return !stack.isEmpty();
    }

    @Override
    public Identifier getStoredSpellId(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(KEY_SPELL_ID, NbtElement.STRING_TYPE)) {
            return Identifier.tryParse(nbt.getString(KEY_SPELL_ID));
        }
        return null;
    }

    @Override
    public List<SpellModifier> getStoredModifiers(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains(KEY_MODIFIERS, NbtElement.LIST_TYPE)) {
            return Collections.emptyList();
        }

        NbtList list = nbt.getList(KEY_MODIFIERS, NbtElement.STRING_TYPE);
        List<SpellModifier> modifiers = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            Identifier id = Identifier.tryParse(list.getString(i));
            if (id != null) {
                // FIX: use ModifierRegistry.get() directly and add if not null
                SpellModifier mod = ModifierRegistry.get(id);
                if (mod != null) {
                    modifiers.add(mod);
                }
            }
        }
        return modifiers;
    }

    @Override
    public void onSpellCast(ItemStack stack, SpellContext ctx) {}

    public static void setSpellId(ItemStack stack, Identifier spellId) {
        stack.getOrCreateNbt().putString(KEY_SPELL_ID, spellId.toString());
    }

    public static void clearSpell(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) nbt.remove(KEY_SPELL_ID);
    }

    public static void setModifiers(ItemStack stack, List<Identifier> modifierIds) {
        NbtList list = new NbtList();
        modifierIds.forEach(id -> list.add(NbtString.of(id.toString())));
        stack.getOrCreateNbt().put(KEY_MODIFIERS, list);
    }

    public static void addModifier(ItemStack stack, Identifier modifierId) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList list = nbt.contains(KEY_MODIFIERS, NbtElement.LIST_TYPE)
                ? nbt.getList(KEY_MODIFIERS, NbtElement.STRING_TYPE)
                : new NbtList();
        list.add(NbtString.of(modifierId.toString()));
        nbt.put(KEY_MODIFIERS, list);
    }

    public static void clearModifiers(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt != null) nbt.remove(KEY_MODIFIERS);
    }
}