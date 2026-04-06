package dev.overgrown.thaumaturge.networking;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import dev.overgrown.thaumaturge.spell.component.GauntletCastHelper;
import dev.overgrown.thaumaturge.spell.input.ComboPattern;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * C2S packet for gauntlet combo casting.
 * The client detects the 3-input combo and sends the matched slot index to the server.
 */
public final class GauntletCastPackets {

    public static final Identifier GAUNTLET_CAST = Thaumaturge.identifier("gauntlet_cast");

    private GauntletCastPackets() {}

    /**
     * Client-side: sends the completed combo's slot index to the server.
     */
    public static void sendCast(int slotIndex) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeVarInt(slotIndex);
        ClientPlayNetworking.send(GAUNTLET_CAST, buf);
    }

    /**
     * Registers the server-side packet receiver. Call from mod init.
     */
    public static void registerServerReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(GAUNTLET_CAST, (server, player, handler, buf, responseSender) -> {
            int slotIndex = buf.readVarInt();

            server.execute(() -> {
                ItemStack stack = player.getMainHandStack();
                if (!(stack.getItem() instanceof ResonanceGauntletItem gauntlet)) return;

                // Validate slot index
                if (slotIndex < 0 || slotIndex >= gauntlet.getFocusSlots()) return;
                if (slotIndex >= ComboPattern.ALL.size()) return;

                ComboPattern pattern = ComboPattern.ALL.get(slotIndex);
                Thaumaturge.LOGGER.info("[Gauntlet] {} sent combo {} via packet",
                        player.getName().getString(), pattern);

                GauntletCastHelper.tryFire(stack, gauntlet.getFoci(stack), pattern, player);
            });
        });
    }
}
