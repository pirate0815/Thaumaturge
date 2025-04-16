package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.function.UnaryOperator;

public class ModComponents {
    public static void register() {
        Thaumaturge.LOGGER.info("Registering Mod Components for " + Thaumaturge.MOD_ID);
    }

    private static <T> ComponentType<T> register(String path, UnaryOperator<ComponentType.Builder<T>> operator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, Thaumaturge.identifier(path), operator.apply(new ComponentType.Builder<>()).build());
    }

    public static final ComponentType<Integer> MAX_FOCI = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Thaumaturge.identifier("max_foci"),
            ComponentType.<Integer>builder().codec(Codec.INT).build()
    );

    public static final ComponentType<GauntletComponent> GAUNTLET_STATE = register("gauntlet_state", builder -> builder
            .codec(GauntletComponent.CODEC)
            .packetCodec(GauntletComponent.PACKET_CODEC));

    public static final ComponentType<BookStateComponent> BOOK_STATE = register("book_state", builder -> builder
        .codec(BookStateComponent.CODEC)
        .packetCodec(BookStateComponent.PACKET_CODEC));

    public static final ComponentType<AspectComponent> ASPECT = register("aspects", builder -> builder
        .codec(AspectComponent.CODEC)
        .packetCodec(AspectComponent.PACKET_CODEC));

}
