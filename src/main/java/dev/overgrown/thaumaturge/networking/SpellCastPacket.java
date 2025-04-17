package dev.overgrown.thaumaturge.networking;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.impl.aer.AdvancedAerLaunch;
import dev.overgrown.thaumaturge.spell.impl.aer.GreaterAerBurst;
import dev.overgrown.thaumaturge.spell.impl.aer.LesserAerBoost;
import dev.overgrown.thaumaturge.spell.impl.motus.LesserMotusBoost;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SpellCastPacket(Type type) implements CustomPayload {
    public static final CustomPayload.Id<SpellCastPacket> ID = new CustomPayload.Id<>(Thaumaturge.identifier("spell_cast"));
    public static final PacketCodec<ByteBuf, SpellCastPacket> PACKET_CODEC = PacketCodec.tuple(
            Type.PACKET_CODEC, SpellCastPacket::type,
            SpellCastPacket::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public enum Type {
        LESSER_AER(LesserAerBoost.ID),
        ADVANCED_AER(AdvancedAerLaunch.ID),
        GREATER_AER(GreaterAerBurst.ID),
        LESSER_MOTUS(LesserMotusBoost.ID);

        private final Identifier spellId;

        Type(Identifier spellId) {
            this.spellId = spellId;
        }

        public Identifier getSpellId() {
            return spellId;
        }

        public static final PacketCodec<ByteBuf, Type> PACKET_CODEC = PacketCodecs.indexed(
                index -> values()[index],
                Type::ordinal
        );
    }
}