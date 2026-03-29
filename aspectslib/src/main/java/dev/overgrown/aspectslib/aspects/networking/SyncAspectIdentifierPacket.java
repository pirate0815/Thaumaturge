package dev.overgrown.aspectslib.aspects.networking;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.aspectslib.aspects.data.Aspect;
import dev.overgrown.aspectslib.aspects.data.AspectManager;
import dev.overgrown.aspectslib.aspects.data.ModRegistries;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * This packet synchronizes both the aspect name mappings and the actual aspect data from server to client.
 * <p>
 * Responsibilities:
 * <ol type="1">
 *     <li>Serialize/deserialize aspect data for network transmission</li>
 *     <li>Send aspect registry to clients/li>
 * </ol>
 * </p>
 * <p>
 * Usage:
 * <li>Called during player join (see AspectsLib.java)</li>
 * <li>Used internally by library</li>
 * </p>
 * <br>
 * <p>
 * Important Connections:
 * <li>{@link AspectsLib}: Main mod class triggers sending</li>
 * <li>{@link ModRegistries}: Source of aspect data</li>
 * <li>{@link Aspect}: Aspect serialization logic</li>
 * </p>
 */
public class SyncAspectIdentifierPacket {
    public static final Identifier ID = AspectsLib.identifier("sync_aspect_packet");

    /**
     * Writes the name-to-ID mapping to the buffer
     */
    public static void writeNameMap(PacketByteBuf buf, Map<String, Identifier> nameMap) {
        buf.writeInt(nameMap.size());
        for (Map.Entry<String, Identifier> entry : nameMap.entrySet()) {
            buf.writeString(entry.getKey());
            buf.writeIdentifier(entry.getValue());
        }
    }

    /**
     * Writes the full aspect data to the buffer
     */
    public static void writeAspectData(PacketByteBuf buf, Map<Identifier, Aspect> aspectMap) {
        buf.writeInt(aspectMap.size());
        for (Map.Entry<Identifier, Aspect> entry : aspectMap.entrySet()) {
            buf.writeIdentifier(entry.getKey());
            Aspect.PACKET_CODEC.encode(buf, entry.getValue());
        }
    }

    /**
     * Writes both the name mapping and aspect data to the buffer
     */
    public static void writeFullData(PacketByteBuf buf, Map<String, Identifier> nameMap, Map<Identifier, Aspect> aspectMap) {
        writeNameMap(buf, nameMap);
        writeAspectData(buf, aspectMap);
    }

    /**
     * Reads the name-to-ID mapping from the buffer
     */
    public static Map<String, Identifier> readNameMap(PacketByteBuf buf) {
        int size = buf.readInt();
        Map<String, Identifier> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = buf.readString();
            Identifier value = buf.readIdentifier();
            map.put(key, value);
        }
        return map;
    }

    /**
     * Reads the aspect data from the buffer
     */
    public static Map<Identifier, Aspect> readAspectData(PacketByteBuf buf) {
        int size = buf.readInt();
        Map<Identifier, Aspect> map = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            Aspect aspect = Aspect.PACKET_CODEC.decode(buf);
            map.put(id, aspect);
        }
        return map;
    }

    /**
     * Sends the full aspect data (names + aspects) from server to client (Send full data to specific player)
     */
    public static void sendFullData(ServerPlayerEntity player, Map<String, Identifier> nameMap, Map<Identifier, Aspect> aspectMap) {
        PacketByteBuf buf = PacketByteBufs.create();
        writeFullData(buf, nameMap, aspectMap);
        ServerPlayNetworking.send(player, ID, buf);
    }

    /**
     * Sends all current aspect data from server to client (Send current registry data to player)
     */
    public static void sendAllData(ServerPlayerEntity player) {
        AspectsLib.LOGGER.debug("Sending {} aspects and {} name mappings to client", 
                ModRegistries.ASPECTS.size(), AspectManager.NAME_TO_ID.size());
        sendFullData(player, AspectManager.NAME_TO_ID, ModRegistries.ASPECTS);
    }
}