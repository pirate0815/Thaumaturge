package dev.overgrown.aspectslib.aspects.networking;

import dev.overgrown.aspectslib.aspects.data.Aspect;
import net.minecraft.network.PacketByteBuf;

/**
 * Simple packet serialization interface for 1.20.1 compatibility.
 * <p>
 * Usage:
 * <li>Implement this for custom data types needing network transmission</li>
 * <li>Used by {@link Aspect} for network serialization</li>
 * <p>
 * Example:
 * <pre>{@code
 * public enum ExampleCodec implements PacketCodec<MyData> {
 *   INSTANCE;
 *   public MyData decode(PacketByteBuf buf) {
 *     return new MyData(buf.readString());
 *   }
 *   public void encode(PacketByteBuf buf, MyData value) {
 *     buf.writeString(value.name());
 *   }
 * }
 * }</pre>
 *
 * @param <T> The type to encode/decode
 */
public interface PacketCodec<T> {
    /**
     * Decodes a value from the buffer
     * @param buf The packet buffer to read from
     * @return The decoded value
     */
    T decode(PacketByteBuf buf);

    /**
     * Encodes a value to the buffer
     * @param buf The packet buffer to write to
     * @param value The value to encode
     */
    void encode(PacketByteBuf buf, T value);
}