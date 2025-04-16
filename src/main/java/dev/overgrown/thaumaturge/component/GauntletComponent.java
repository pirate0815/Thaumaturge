package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record GauntletComponent(List<Identifier> fociIds) {
    public static final Codec<GauntletComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Identifier.CODEC).fieldOf("foci_ids").forGetter(GauntletComponent::fociIds)
    ).apply(instance, GauntletComponent::new));

    public static final PacketCodec<ByteBuf, GauntletComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, Identifier.PACKET_CODEC), GauntletComponent::fociIds,
            GauntletComponent::new
    );

    public static final GauntletComponent DEFAULT = new GauntletComponent(List.of());

    public int fociCount() {
        return fociIds.size();
    }
}