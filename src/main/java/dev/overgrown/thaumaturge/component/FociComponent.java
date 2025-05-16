package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record FociComponent(Identifier aspectId, Identifier modifierId) {
    public static final Codec<FociComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("aspect").forGetter(FociComponent::aspectId),
                    Identifier.CODEC.optionalFieldOf("modifier", Thaumaturge.identifier("simple")).forGetter(FociComponent::modifierId)
            ).apply(instance, FociComponent::new)
    );

    public static final PacketCodec<RegistryByteBuf, FociComponent> PACKET_CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC,
            FociComponent::aspectId,
            Identifier.PACKET_CODEC,
            FociComponent::modifierId,
            FociComponent::new
    );

    public static final FociComponent DEFAULT = new FociComponent(null, Thaumaturge.identifier("simple_resonance_modifier"));
}