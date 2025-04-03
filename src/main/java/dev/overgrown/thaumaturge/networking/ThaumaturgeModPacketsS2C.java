package dev.overgrown.thaumaturge.networking;

import dev.overgrown.thaumaturge.data.AspectManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.CustomPayload;

public class ThaumaturgeModPacketsS2C {

    /**
     * This registers the packet to sync aspect identifiers.
     */
    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(SyncAspectIdentifierPacket.ID, ThaumaturgeModPacketsS2C::handle);
    }

    /**
     * This will set the AspectManager.NAME_TO_ID to match that of the server side.
     */
    private static void handle(CustomPayload customPayload, ClientPlayNetworking.Context context) {
        SyncAspectIdentifierPacket packet = ((SyncAspectIdentifierPacket) customPayload);
        MinecraftClient client = context.client();
        client.execute(() -> AspectManager.NAME_TO_ID = packet.map());
    }
}
