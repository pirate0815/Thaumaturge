package dev.overgrown.thaumaturge.networking;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the packet that is used to sync the aspect identifiers from the server to the client.
 */
public record SyncAspectIdentifierPacket(Map<String, Identifier> map) implements CustomPayload {
    public static final Id<SyncAspectIdentifierPacket> ID = new Id<>(Thaumaturge.identifier("sync_aspect_packet"));
    public static final PacketCodec<? super RegistryByteBuf, SyncAspectIdentifierPacket> PACKET_CODEC = PacketCodec.of(SyncAspectIdentifierPacket::toBuffer, SyncAspectIdentifierPacket::fromBuffer);

    /**
     * This writes up all the data from the map into a PacketByteBuf to send to the client.
     */
    public void toBuffer(PacketByteBuf buf) {
        buf.writeInt(map.size());
        for (Map.Entry<String, Identifier> entry : map.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeIdentifier(entry.getValue());
        }
    }

    /**
     * This takes the PacketByteBuf that is sent to the client, and decodes it back into a map.
     */
    public static SyncAspectIdentifierPacket fromBuffer(PacketByteBuf buf) {
        int size = buf.readInt();
        Map<String, Identifier> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readString();
            Identifier value = buf.readIdentifier();
            map.put(key, value);
        }
        return new SyncAspectIdentifierPacket(map);
    }

    /**
     * This sends a packet from the server to the client in order to sync the maps.
     */
    public static void sendMap(ServerPlayerEntity player, Map<String, Identifier> map) {
        SyncAspectIdentifierPacket packet = new SyncAspectIdentifierPacket(map);
        ServerPlayNetworking.send(player, packet);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}