/**
 * ThaumaturgeModPacketsS2C.java
 * <p>
 * This class handles server-to-client (S2C) packet registration and processing.
 * Currently, it only manages the synchronization of aspect identifiers between
 * server and client to ensure consistent aspect data.
 *
 * @see dev.overgrown.thaumaturge.networking.SyncAspectIdentifierPacket
 * @see dev.overgrown.thaumaturge.data.AspectManager
 */
package dev.overgrown.thaumaturge.networking;

import dev.overgrown.thaumaturge.data.AspectManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.CustomPayload;

public class ThaumaturgeModPacketsS2C {

    /**
     * Registers all server-to-client packets used by the mod
     * Currently only registers the SyncAspectIdentifierPacket
     */
    public static void register() {
        // Register the global receiver for aspect identifiers sync
        ClientPlayNetworking.registerGlobalReceiver(SyncAspectIdentifierPacket.ID, ThaumaturgeModPacketsS2C::handle);
    }

    /**
     * Handles received SyncAspectIdentifierPacket
     * Updates the client's aspect name-to-ID map to match the server's map
     *
     * @param customPayload The received packet payload
     * @param context The networking context
     */
    private static void handle(CustomPayload customPayload, ClientPlayNetworking.Context context) {
        // Cast the payload to the expected packet type
        SyncAspectIdentifierPacket packet = ((SyncAspectIdentifierPacket) customPayload);
        // Retrieve the MinecraftClient instance using the singleton getter
        MinecraftClient client = MinecraftClient.getInstance();

        // Execute on the client thread to update the aspect manager
        client.execute(() -> AspectManager.NAME_TO_ID = packet.map());
    }
}