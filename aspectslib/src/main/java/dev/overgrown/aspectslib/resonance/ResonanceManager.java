package dev.overgrown.aspectslib.resonance;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.aspectslib.AspectsLib;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

public class ResonanceManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
    public static final Map<Identifier, List<Resonance>> RESONANCE_MAP = new HashMap<>();
    private static final Gson GSON = new Gson();
    private static final Codec<Resonance> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("aspect1").forGetter(Resonance::aspect1),
                    Identifier.CODEC.fieldOf("aspect2").forGetter(Resonance::aspect2),
                    Codec.STRING.fieldOf("type").xmap(
                            s -> Resonance.Type.valueOf(s.toUpperCase()),
                            type -> type.name().toLowerCase()
                    ).forGetter(Resonance::type),
                    Codec.DOUBLE.optionalFieldOf("factor", 1.5).forGetter(Resonance::factor)
            ).apply(instance, Resonance::new)
    );

    public ResonanceManager() {
        super(GSON, "resonance");
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        RESONANCE_MAP.clear();

        AspectsLib.LOGGER.info("Found {} resonance files", prepared.size());

        prepared.forEach((id, json) -> {
            AspectsLib.LOGGER.info("Processing resonance file: {}", id);

            CODEC.parse(JsonOps.INSTANCE, json)
                    .resultOrPartial(error -> AspectsLib.LOGGER.error("Failed to parse resonance file {}: {}", id, error))
                    .ifPresent(resonance -> {
                        AspectsLib.LOGGER.info("Loaded resonance: {} <-> {} ({})",
                                resonance.aspect1(), resonance.aspect2(), resonance.type());

                        RESONANCE_MAP.computeIfAbsent(resonance.aspect1(), k -> new ArrayList<>()).add(resonance);
                        RESONANCE_MAP.computeIfAbsent(resonance.aspect2(), k -> new ArrayList<>()).add(resonance);
                    });
        });

        AspectsLib.LOGGER.info("Loaded resonance relationships for {} aspects", RESONANCE_MAP.size());
    }

    @Override
    public Identifier getFabricId() {
        return AspectsLib.identifier("resonance");
    }

    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(AspectsLib.identifier("aspects"));
    }
}