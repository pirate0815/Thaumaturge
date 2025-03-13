package dev.overgrown.thaumaturge.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record Aspect(String name) {

    public static final Codec<Aspect> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.STRING.fieldOf("name").forGetter(Aspect::name)
                    )
                    .apply(instance, Aspect::new)
    );
}
