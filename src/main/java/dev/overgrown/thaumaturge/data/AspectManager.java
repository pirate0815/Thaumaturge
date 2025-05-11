package dev.overgrown.thaumaturge.data;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * This class is where the Json Data is converted into Aspect classes and added to the Aspect registry.
 * Most of this is done automatically (See the "super" in the constructor).
 */
public class AspectManager extends JsonDataLoader<Aspect> implements IdentifiableResourceReloadListener {

    RegistryWrapper.WrapperLookup wrapperLookup;

    /**
     * This parameter holds the Names of aspects against their registration Identifiers.
     */
    public static Map<String, Identifier> NAME_TO_ID = new HashMap<>();

    /**
     * Instantiate the AspectManager and cache the registry lookup to use after data has been loaded.
     */
    public AspectManager(RegistryWrapper.WrapperLookup wrapperLookup) {
        super(wrapperLookup, Aspect.CODEC, ModRegistries.ASPECTS);
        this.wrapperLookup = wrapperLookup;
    }


    /**
     *  This method will construct a Map<String, Identifier>, pairing the names of aspects, with their Identifiers.
     */
    @Override
    protected void apply(Map<Identifier, Aspect> prepared, ResourceManager manager, Profiler profiler) {
        NAME_TO_ID.clear();
        Optional<? extends RegistryWrapper.Impl<Aspect>> optionalRegistry = this.wrapperLookup.getOptional(ModRegistries.ASPECTS);
        optionalRegistry.ifPresent((aspectRegistry) -> {
            Thaumaturge.LOGGER.info("Finished loading aspects from data files. Registry contains {} aspects.", aspectRegistry.streamEntries().toArray().length);
            aspectRegistry.streamEntries().forEach((aspectReference -> NAME_TO_ID.put(aspectReference.value().name(), aspectReference.registryKey().getValue())));
        });
    }

    @Override
    public Identifier getFabricId() {
        return Thaumaturge.identifier("aspects");
    }
}
