package dev.overgrown.thaumaturge.component;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModComponents {
    public static void register() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Thaumaturge.identifier("aspects"), AspectComponent.TYPE);
    }
}