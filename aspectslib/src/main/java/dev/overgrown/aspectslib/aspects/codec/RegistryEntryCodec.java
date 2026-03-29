package dev.overgrown.aspectslib.aspects.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

/**
 * Codec for RegistryEntry serialization (currently unused in AspectsLib).
 * <p>
 * Provided as utility for possible future use.
 * </p>
 */
public class RegistryEntryCodec<T> implements Codec<RegistryEntry<T>> {
    private final Registry<T> registry;

    public RegistryEntryCodec(Registry<T> registry) {
        this.registry = registry;
    }

    @Override
    public <S> DataResult<Pair<RegistryEntry<T>, S>> decode(DynamicOps<S> ops, S input) {
        return Identifier.CODEC.decode(ops, input).flatMap(idPair -> {
            Identifier id = idPair.getFirst();
            RegistryKey<T> key = RegistryKey.of(registry.getKey(), id);
            
            return registry.getEntry(key)
                    .<DataResult<Pair<RegistryEntry<T>, S>>>map(entry ->
                            DataResult.success(Pair.of((RegistryEntry<T>) entry, idPair.getSecond()))
                    )
                    .orElseGet(() -> {
                        T value = registry.get(id);
                        if (value != null) {
                            return registry.getEntry(registry.getRawId(value))
                                    .<DataResult<Pair<RegistryEntry<T>, S>>>map(entry ->
                                            DataResult.success(Pair.of((RegistryEntry<T>) entry, idPair.getSecond()))
                                    )
                                    .orElseGet(() -> {
                                        RegistryEntry<T> entry = RegistryEntry.of(value);
                                        return DataResult.success(Pair.of(entry, idPair.getSecond()));
                                    });
                        }
                        return DataResult.error(() -> "Unknown registry key: " + id);
                    });
        });
    }

    @Override
    public <S> DataResult<S> encode(RegistryEntry<T> input, DynamicOps<S> ops, S prefix) {
        return input.getKey()
                .map(key -> Identifier.CODEC.encode(key.getValue(), ops, prefix))
                .orElseGet(() -> {
                    if (input.hasKeyAndValue()) {
                        Identifier id = registry.getId(input.value());
                        if (id != null) {
                            return Identifier.CODEC.encode(id, ops, prefix);
                        }
                    }
                    return DataResult.error(() -> "Registry entry has no key or value");
                });
    }
}