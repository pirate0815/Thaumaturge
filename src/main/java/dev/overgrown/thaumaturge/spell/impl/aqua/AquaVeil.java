package dev.overgrown.thaumaturge.spell.impl.aqua;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.effect.ModStatusEffects;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class AquaVeil implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("aqua_veil");

    @Override
    public void execute(ServerPlayerEntity caster) {
        // Apply Water Breathing
        caster.addStatusEffect(new StatusEffectInstance(
                StatusEffects.WATER_BREATHING, 1200, 0, false, false, true
        ));

        // Apply Aqua Veil effect using stored registry entry
        caster.addStatusEffect(new StatusEffectInstance(
                ModStatusEffects.AQUA_VEIL, 1200, 0, false, false, true
        ));
    }
}