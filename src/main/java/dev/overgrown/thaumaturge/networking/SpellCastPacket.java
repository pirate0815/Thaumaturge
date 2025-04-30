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
import dev.overgrown.thaumaturge.spell.impl.aer.AdvancedAerLaunch;
import dev.overgrown.thaumaturge.spell.impl.aer.GreaterAerBurst;
import dev.overgrown.thaumaturge.spell.impl.aer.LesserAerBoost;
import dev.overgrown.thaumaturge.spell.impl.aqua.AquaBoost;
import dev.overgrown.thaumaturge.spell.impl.gelum.FrozenStep;
import dev.overgrown.thaumaturge.spell.impl.motus.LesserMotusBoost;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpellCastPacket(Type type) implements CustomPayload {
    /**
     * Unique identifier for this packet type
     */
    public static final CustomPayload.Id<SpellCastPacket> ID = new CustomPayload.Id<>(Thaumaturge.identifier("spell_cast"));

    /**
     * Codec for serializing/deserializing this packet
     */
    public static final PacketCodec<ByteBuf, SpellCastPacket> PACKET_CODEC = PacketCodec.tuple(
            Type.PACKET_CODEC, SpellCastPacket::type,
            SpellCastPacket::new
    );

    /**
     * Gets the packet ID for network handling
     *
     * @return This packet's identifier
     */
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    /**
     * Enum defining available spell types that can be cast
     * Each type is mapped to a specific spell ID that corresponds to
     * implementations in the spell registry
     */
    public enum Type {
        LESSER_AER(LesserAerBoost.ID),
        LESSER_AQUA(AquaBoost.ID),
        LESSER_GELUM(FrozenStep.ID),
        LESSER_MOTUS(LesserMotusBoost.ID),
        ADVANCED_AER(AdvancedAerLaunch.ID),
        GREATER_AER(GreaterAerBurst.ID);

        private final Identifier spellId;

        /**
         * Constructor that associates the enum value with a spell identifier
         *
         * @param spellId The identifier of the spell this type represents
         */
        Type(Identifier spellId) {
            this.spellId = spellId;
        }

        /**
         * Gets the spell identifier associated with this type
         *
         * @return The spell identifier
         */
        public Identifier getSpellId() {
            return spellId;
        }

        /**
         * Codec for serializing/deserializing spell types in packets
         * Uses an index-based approach to minimize packet size
         */
        public static final PacketCodec<ByteBuf, Type> PACKET_CODEC = PacketCodecs.indexed(
                index -> values()[index],
                Type::ordinal
        );
    }
}