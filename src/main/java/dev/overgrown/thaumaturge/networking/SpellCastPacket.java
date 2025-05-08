package dev.overgrown.thaumaturge.networking;

import com.mojang.serialization.Codec;
import dev.overgrown.thaumaturge.Thaumaturge;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SpellCastPacket(SpellTier tier) implements CustomPayload {
    public static final CustomPayload.Id<SpellCastPacket> ID = new CustomPayload.Id<>(Thaumaturge.identifier("spell_cast"));
    public static final PacketCodec<ByteBuf, SpellCastPacket> PACKET_CODEC = PacketCodec.tuple(
            SpellTier.PACKET_CODEC, SpellCastPacket::tier,
            SpellCastPacket::new
    );

    public enum SpellTier {
        LESSER,
        ADVANCED,
        GREATER;

        private static final SpellTier[] VALUES = values();

        public static final PacketCodec<ByteBuf, SpellTier> PACKET_CODEC = PacketCodecs.indexed(
                index -> VALUES[index],
                SpellTier::ordinal
        );

        public static final Codec<SpellTier> CODEC = Codec.STRING.xmap(
                SpellTier::valueOf,
                SpellTier::name
        );
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}