package dev.overgrown.thaumaturge.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.data.Aspect;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * The `ItemAspectComponent` class manages the association between items and their aspects.
 * It stores the levels of aspects and provides functionality for tooltip display,
 * serialization, and manipulation.
 */
public class AspectComponent {

    // Default instance with no aspects
    public static final AspectComponent DEFAULT = new AspectComponent(new Object2IntOpenHashMap<>());

    // Codec for serialization and deserialization
    private static final Codec<Object2IntOpenHashMap<RegistryEntry<Aspect>>> INLINE_CODEC =
            Codec.unboundedMap(Aspect.ENTRY_CODEC, Codec.INT)
                    .xmap(Object2IntOpenHashMap::new, Function.identity());

    private static final Codec<AspectComponent> BASE_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    INLINE_CODEC.fieldOf("aspects").forGetter(component -> component.aspects)
            ).apply(instance, AspectComponent::new)
    );

    public static final Codec<AspectComponent> CODEC = Codec.withAlternative(BASE_CODEC, INLINE_CODEC, AspectComponent::new);

    // Packet codec for network synchronization
    public static final PacketCodec<RegistryByteBuf, AspectComponent> PACKET_CODEC = PacketCodec.tuple(
            PacketCodecs.map(Object2IntOpenHashMap::new, Aspect.ENTRY_PACKET_CODEC, PacketCodecs.VAR_INT),
            component -> component.aspects,
            AspectComponent::new
    );

    // Internal storage for aspects and their levels
    private final Object2IntOpenHashMap<RegistryEntry<Aspect>> aspects;

    // Constructor
    public AspectComponent(Object2IntOpenHashMap<RegistryEntry<Aspect>> aspects) {
        this.aspects = aspects;
    }

    /**
     * Gets the level of the specified aspect.
     *
     * @param aspect The aspect to query.
     * @return The level of the aspect, or 0 if not present.
     */
    public int getLevel(RegistryEntry<Aspect> aspect) {
        return this.aspects.getInt(aspect);
    }

    /**
     * Gets the set of registered aspects.
     *
     * @return An unmodifiable set of aspects.
     */
    public Set<RegistryEntry<Aspect>> getAspects() {
        return Collections.unmodifiableSet(this.aspects.keySet());
    }

    public Object2IntOpenHashMap<RegistryEntry<Aspect>> getMap() {
        return this.aspects;
    }

    /**
     * Gets the number of registered aspects.
     *
     * @return The number of aspects.
     */
    public int getSize() {
        return this.aspects.size();
    }

    /**
     * Checks if there are no aspects registered.
     *
     * @return `true` if no aspects are registered, otherwise `false`.
     */
    public boolean isEmpty() {
        return this.aspects.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof AspectComponent other) {
            return this.aspects.equals(other.aspects);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ItemAspectComponent{aspects=" + this.aspects + "}";
    }

    public AspectComponent addAspect(AspectComponent aspectComponent) {
        for (Object2IntMap.Entry<RegistryEntry<Aspect>> aspectIntegerEntry : aspectComponent.aspects.object2IntEntrySet()) {
            this.aspects.put(aspectIntegerEntry.getKey(), this.aspects.getOrDefault(aspectIntegerEntry.getKey(), 0) + aspectIntegerEntry.getIntValue());
        }
        return this;
    }

    /**
     * Builder class for creating `ItemAspectComponent` instances.
     */
    public static class Builder {
        private final Object2IntOpenHashMap<RegistryEntry<Aspect>> aspects = new Object2IntOpenHashMap<>();

        public Builder(AspectComponent component) {
            this.aspects.putAll(component.aspects);
        }

        /**
         * Sets the level of an aspect.
         *
         * @param aspect The aspect to set.
         * @param level  The level to set.
         */
        public void set(RegistryEntry<Aspect> aspect, int level) {
            if (level <= 0) {
                this.aspects.removeInt(aspect);
            } else {
                this.aspects.put(aspect, level);
            }
        }

        /**
         * Adds a level to an aspect.
         *
         * @param aspect The aspect to add.
         * @param level  The level to add.
         */
        public void add(RegistryEntry<Aspect> aspect, int level) {
            if (level > 0) {
                this.aspects.merge(aspect, level, Integer::sum);
            }
        }

        /**
         * Removes aspects that match the given predicate.
         *
         * @param predicate The predicate to test.
         */
        public void remove(Predicate<RegistryEntry<Aspect>> predicate) {
            this.aspects.keySet().removeIf(predicate);
        }

        /**
         * Builds the `ItemAspectComponent`.
         *
         * @return The constructed `ItemAspectComponent`.
         */
        public AspectComponent build() {
            return new AspectComponent(this.aspects);
        }
    }
}
