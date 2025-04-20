package dev.overgrown.thaumaturge.effect;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;

public class ModStatusEffects {
    public static final RegistryEntry.Reference<StatusEffect> AQUA_VEIL = registerEffect("aqua_veil", new AquaVeilEffect());
    public static final RegistryEntry.Reference<StatusEffect> DEHYDRATED = registerEffect("dehydrated", new DehydratedEffect());

    public static void register() {
        // Registration is handled through the registerEffect calls
    }

    private static RegistryEntry.Reference<StatusEffect> registerEffect(String id, StatusEffect effect) {
        return Registry.registerReference(
                Registries.STATUS_EFFECT,
                Thaumaturge.identifier(id),
                effect
        );
    }
}