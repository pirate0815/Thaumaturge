package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Identifier;

public record FociComponent(SpellCastPacket.SpellTier tier, Identifier aspectId) {
    public static final Codec<FociComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.xmap(SpellCastPacket.SpellTier::valueOf, SpellCastPacket.SpellTier::name).fieldOf("tier").forGetter(FociComponent::tier),
                    Identifier.CODEC.fieldOf("aspect").forGetter(FociComponent::aspectId)
            ).apply(instance, FociComponent::new)
    );

    public static final PacketCodec<RegistryByteBuf, FociComponent> PACKET_CODEC = PacketCodec.tuple(
            SpellCastPacket.SpellTier.PACKET_CODEC.<RegistryByteBuf>cast(),
            FociComponent::tier,
            Identifier.PACKET_CODEC,
            FociComponent::aspectId,
            FociComponent::new
    );

    public static final FociComponent DEFAULT = new FociComponent(null, null);
}