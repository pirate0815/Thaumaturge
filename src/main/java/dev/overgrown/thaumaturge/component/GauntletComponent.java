/**
 * GauntletComponent.java
 * <p>
 * This class represents the state of a Thaumaturge gauntlet, specifically tracking
 * which foci are equipped in it.
 * <p>
 * A gauntlet can hold multiple foci, which enable spellcasting capabilities.
 * This component stores the list of foci by their registry identifiers.
 * <p>
 * The component supports serialization for saving/loading (CODEC) and networking (PACKET_CODEC)
 * to ensure the gauntlet state is properly synchronized between client and server.
 *
 * @see dev.overgrown.thaumaturge.component.ModComponents
 * @see dev.overgrown.thaumaturge.spell.SpellHandler
 */
package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public record GauntletComponent(List<Identifier> fociIds) {
    /**
     * Codec for serializing and deserializing the component when saving/loading the world
     */
    public static final Codec<GauntletComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Identifier.CODEC).fieldOf("foci_ids").forGetter(GauntletComponent::fociIds)
    ).apply(instance, GauntletComponent::new));

    /**
     * Packet codec for network synchronization between client and server
     */
    public static final PacketCodec<ByteBuf, GauntletComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.collection(ArrayList::new, Identifier.PACKET_CODEC), GauntletComponent::fociIds,
            GauntletComponent::new
    );

    /**
     * Default empty state for gauntlets with no foci
     */
    public static final GauntletComponent DEFAULT = new GauntletComponent(List.of());

    /**
     * Returns the number of foci currently equipped in the gauntlet
     *
     * @return The count of equipped foci
     */
    public int fociCount() {
        return fociIds.size();
    }
}