package dev.overgrown.aspectslib.spell.networking;

import dev.overgrown.aspectslib.AspectsLib;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * Client-to-server packet sent when a player triggers a spell cast via their
 * conduit (gauntlet).  Carries the cast mode and which hand is holding the conduit.
 *
 * <h3>Cast modes</h3>
 * <ul>
 *   <li>{@link CastMode#SELF}     – right-click; spell targets the caster.
 *       Requires a Lesser Focus in the gauntlet.</li>
 *   <li>{@link CastMode#TARGETED} – left-click; spell is aimed at the
 *       crosshair target. Requires an Advanced Focus.</li>
 *   <li>{@link CastMode#AOE}      – middle-click; spell detonates as an
 *       area-of-effect around the cast origin.  Requires a Greater Focus and
 *       carries an inherent stability penalty.</li>
 * </ul>
 */
public final class SpellCastC2SPacket {

    public static final Identifier ID = AspectsLib.identifier("spell_cast");

    // ------------------------------------------------------------------ //
    // Cast mode                                                            //
    // ------------------------------------------------------------------ //

    public enum CastMode {
        /** Right-click – target self (Lesser Focus). */
        SELF,
        /** Left-click  – target entity / block in sight (Advanced Focus). */
        TARGETED,
        /** Middle-click – area-of-effect (Greater Focus). */
        AOE
    }

    // ------------------------------------------------------------------ //
    // Fields                                                               //
    // ------------------------------------------------------------------ //

    private final CastMode mode;
    /** 0 = main hand, 1 = offhand. */
    private final int hand;

    // ------------------------------------------------------------------ //
    // Construction                                                         //
    // ------------------------------------------------------------------ //

    public SpellCastC2SPacket(CastMode mode, int hand) {
        this.mode = mode;
        this.hand = hand;
    }

    // ------------------------------------------------------------------ //
    // Serialisation                                                        //
    // ------------------------------------------------------------------ //

    public static SpellCastC2SPacket read(PacketByteBuf buf) {
        CastMode mode = buf.readEnumConstant(CastMode.class);
        int hand      = buf.readByte();
        return new SpellCastC2SPacket(mode, hand);
    }

    public void write(PacketByteBuf buf) {
        buf.writeEnumConstant(mode);
        buf.writeByte(hand);
    }

    /** Helper: builds the ready-to-send buffer for this packet. */
    public PacketByteBuf toBuffer() {
        PacketByteBuf buf = PacketByteBufs.create();
        write(buf);
        return buf;
    }

    // ------------------------------------------------------------------ //
    // Accessors                                                            //
    // ------------------------------------------------------------------ //

    public CastMode getMode() { return mode; }
    public int      getHand() { return hand; }

    @Override
    public String toString() {
        return "SpellCastC2SPacket{mode=" + mode + ", hand=" + hand + "}";
    }
}