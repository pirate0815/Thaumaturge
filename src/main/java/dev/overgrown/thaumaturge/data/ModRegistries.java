package dev.overgrown.thaumaturge.data;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.entity.effect.StatusEffect;

public class ModRegistries {
    public static final RegistryKey<Registry<Aspect>> ASPECTS =
            RegistryKey.ofRegistry(Thaumaturge.identifier("aspects"));

    // Add status effect registry
    public static final RegistryKey<StatusEffect> AQUA_VEIL_EFFECT =
            RegistryKey.of(RegistryKeys.STATUS_EFFECT, Thaumaturge.identifier("aqua_veil"));
    public static final RegistryKey<StatusEffect> DEHYDRATED_EFFECT =
            RegistryKey.of(RegistryKeys.STATUS_EFFECT, Thaumaturge.identifier("dehydrated"));

    public static void register() {
        DynamicRegistries.registerSynced(ASPECTS, Aspect.CODEC);
    }
}