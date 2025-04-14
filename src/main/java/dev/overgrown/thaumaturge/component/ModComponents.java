package dev.overgrown.thaumaturge.component;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModComponents {
    public static final ComponentType<NbtComponent> BOOK_STATE = ComponentType.<NbtComponent>builder()
            .codec(NbtComponent.CODEC)
            .build();

    public static void register() {
        Registry.register(Registries.DATA_COMPONENT_TYPE, Thaumaturge.identifier("aspects"), AspectComponent.TYPE);
        Registry.register(Registries.DATA_COMPONENT_TYPE, Thaumaturge.identifier("book_state"), BOOK_STATE);
    }
}