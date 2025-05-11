package dev.overgrown.thaumaturge.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

/**
 * This is the Aspect class, which holds an instance of Aspect derived from files found in
 * data/thaumaturge/thaumaturge/aspects/{json_file}.json
 *
 * @param name            This holds the name of the Aspect, as it should be rendered in displays.
 * @param textureLocation This holds the location of the texture, if the player wishes to override the default texture with one from a specific directory.
 */
public record Aspect(String name, Identifier textureLocation) {
    /**
     * This is the codec for encoding/decoding between Aspects and Json. it currently contains two fields,
     * "name" and "texture location", for the two current data fields. you can add up to 8 fields here,
     * which can be used to add extra data to the Aspect class, such as "connected_aspects" or something of the sort.
     */
    public static final Codec<Aspect> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.STRING.fieldOf("name").forGetter(Aspect::name),
                            Identifier.CODEC.optionalFieldOf("texture_location", Thaumaturge.identifier("empty")).forGetter(Aspect::textureLocation)
                    )
                    .apply(instance, Aspect::new)
    );

    public static final Codec<RegistryEntry<Aspect>> ENTRY_CODEC = RegistryFixedCodec.of(ModRegistries.ASPECTS);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<Aspect>> ENTRY_PACKET_CODEC = PacketCodecs.registryEntry(ModRegistries.ASPECTS);


    /**
     * This, similarly to CODEC, is used to encoding and decoding the Aspect, but instead is used in
     * {@link dev.overgrown.thaumaturge.component.AspectComponent} in order to encode and decode the aspects in a given
     * item's Aspect Component.
     */
    public static final PacketCodec<RegistryByteBuf, Aspect> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, Aspect::name,
            Identifier.PACKET_CODEC, Aspect::textureLocation,
            Aspect::new
    );

    /**
     * The constructor for the Aspect Class.
     *
     * @param name            the name of the Aspect.
     * @param textureLocation the texture location used in rendering.
     */
    public Aspect {
    }

    /**
     * This method will get the text component name of the Aspect, defaulting to the name defined in the Json file
     * in the event that any name defined in the lang files is not found.
     *
     * @return the text component containing the name of the Aspect.
     */
    public MutableText getTranslatedName() {
        return Text.translatableWithFallback(getTranslatableKey(), name);
    }

    /**
     * This method constructs a translation key based on the identifier of the item, or the name of the aspect.
     *
     * @return a string translation key to be used in Text#translatable(String key).
     */
    public String getTranslatableKey() {
        if (getIdentifier() != null) {
            return "aspect." + getIdentifier().getNamespace() + "." + getIdentifier().getPath() + ".name";
        } else {
            return "aspect." + Thaumaturge.MOD_ID + "." + this.name.toLowerCase() + ".name";
        }
    }

    /**
     * A getter for the Aspect's name.
     *
     * @return the name of the Aspect
     */
    @Override
    public String name() {
        return this.name;
    }

    /**
     * A getter for the Aspect's identifier.
     *
     * @return the identifier of the Aspect
     */
    private Identifier getIdentifier() {
        return AspectManager.NAME_TO_ID.get(this.name);
    }

    /**
     * A getter for the Aspect's texture location.
     *
     * @return the identifier that points towards the texture used for the Aspect's rendering
     */
    @Override
    public Identifier textureLocation() {
        if (!this.textureLocation.equals(Thaumaturge.identifier("empty"))) {
            return this.textureLocation;
        } else if (getIdentifier() != null) {
            return Identifier.of(this.getIdentifier().getNamespace(), "textures/aspects_icons/" + this.getIdentifier().getPath() + ".png");
        } else {
            return Thaumaturge.identifier("textures/aspects_icons/" + this.name.toLowerCase() + ".png");
        }
    }

    @Override
    public @NotNull String toString() {
        return this.name;
    }
}
