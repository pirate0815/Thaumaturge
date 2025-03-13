package dev.overgrown.thaumaturge.data;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Map;

public class AspectManager extends JsonDataLoader<Aspect> implements IdentifiableResourceReloadListener {
    private static final ResourceFinder FINDER = ResourceFinder.json("aspects");

    public AspectManager(RegistryWrapper.WrapperLookup wrapperLookup) {
        super(Aspect.CODEC, FINDER);
    }


    @Override
    protected void apply(Map<Identifier, Aspect> prepared, ResourceManager manager, Profiler profiler) {
        prepared.forEach(((identifier, aspect) -> {
            Thaumaturge.LOGGER.info(identifier.toString());
            Thaumaturge.LOGGER.info(aspect.name());
        }));
    }

    @Override
    public Identifier getFabricId() {
        return Thaumaturge.identifier("aspects");
    }
}
