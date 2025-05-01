/**
 * SpellCastPacket.java
 * <p>
 * This class defines the packet sent when a player casts a spell.
 * The packet contains an enum value indicating which type of spell
 * was cast, which is translated to the corresponding spell identifier
 * on the server side.
 * <p>
 * The packet is registered for both client-to-server (player initiating spell cast)
 * and server-to-client (for potential spell effect synchronization).
 *
 * @see dev.overgrown.thaumaturge.ThaumaturgeClient
 * @see dev.overgrown.thaumaturge.Thaumaturge#handleSpellCast
 */
package dev.overgrown.thaumaturge.networking;

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
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}