package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record GauntletComponent(boolean hasFoci) {

    public static final Codec<GauntletComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("has_foci").forGetter(GauntletComponent::hasFoci)
    ).apply(instance, GauntletComponent::new));

    public static final PacketCodec<ByteBuf, GauntletComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.BOOLEAN, GauntletComponent::hasFoci,
            GauntletComponent::new
    );

    public static final GauntletComponent DEFAULT = new GauntletComponent(false);
}