package dev.overgrown.thaumaturge.spell.networking;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

public class SpellCastPacket {
    public static final Identifier ID = Thaumaturge.identifier("spell_cast");

    private final Hand hand;
    private final int spellKey;

    public SpellCastPacket(Hand hand, int spellKey) {
        this.hand = hand;
        this.spellKey = spellKey;
    }

    public SpellCastPacket(PacketByteBuf buf) {
        this.hand = buf.readEnumConstant(Hand.class);
        this.spellKey = buf.readInt();
    }

    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(hand);
        buf.writeInt(spellKey);
    }

    public Hand getHand() {
        return hand;
    }

    public int getSpellKey() {
        return spellKey;
    }
}