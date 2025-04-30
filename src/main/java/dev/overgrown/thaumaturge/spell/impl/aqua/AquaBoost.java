package dev.overgrown.thaumaturge.spell.impl.aqua;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.effect.ModStatusEffects;
import dev.overgrown.thaumaturge.spell.registry.SpellEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.entity.effect.StatusEffectInstance;

public class AquaBoost implements SpellEntry.SpellExecutor {
    public static final Identifier ID = Thaumaturge.identifier("aqua_boost");

    @Override
    public void execute(ServerPlayerEntity caster) {
        caster.addStatusEffect(new StatusEffectInstance(
                ModStatusEffects.AQUA_BOOST, 600, 0, true, true)
        );
    }
}