package dev.overgrown.aspectslib.spell.networking;

import dev.overgrown.aspectslib.AspectsLib;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

/**
 * Registers all spell-related networking channels for AspectsLib.
 *
 * <p>This class opens the {@link SpellCastC2SPacket#ID} channel on the server
 * side. The actual game logic that runs when a packet arrives must be provided
 * by the consuming mod (e.g., Thaumaturge) by calling
 * {@link ServerPlayNetworking#registerGlobalReceiver(net.minecraft.util.Identifier,
 * ServerPlayNetworking.PlayChannelHandler)} with {@link SpellCastC2SPacket#ID}.
 */
public final class SpellNetworking {

    private SpellNetworking() {}

    /**
     * Opens the spell-cast C2S channel so that clients may send
     * {@link SpellCastC2SPacket} packets to the server.
     *
     * <p>Consuming mods must register their own receiver via
     * {@link ServerPlayNetworking#registerGlobalReceiver} using
     * {@link SpellCastC2SPacket#ID} to handle the packet payload.
     */
    public static void init() {
        AspectsLib.LOGGER.info("SpellNetworking: channel {} initialised – " +
                        "consuming mods should register a receiver for this channel.",
                SpellCastC2SPacket.ID);
    }
}