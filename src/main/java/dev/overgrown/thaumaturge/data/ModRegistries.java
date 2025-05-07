package dev.overgrown.thaumaturge.data;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public class ModRegistries {
    public static final RegistryKey<Registry<Aspect>> ASPECTS =
            RegistryKey.ofRegistry(Thaumaturge.identifier("aspects"));

    public static void register() {
        DynamicRegistries.registerSynced(ASPECTS, Aspect.CODEC);
    }
}