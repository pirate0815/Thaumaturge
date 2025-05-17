package dev.overgrown.thaumaturge.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.component.AspectComponent;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.profiler.Profiler;

import java.util.*;

/**
 * This class populates the ItemAspectRegistry, with default integer values for different aspects.
 */
public class CustomItemTagManager extends JsonDataLoader<JsonElement> implements IdentifiableResourceReloadListener {
    /**
     * This field holds a reference to the item tags' directory.
     */
    private static final ResourceFinder FINDER = ResourceFinder.json("tags/item");
    RegistryWrapper.WrapperLookup wrapperLookup;

    /**
     * Instantiate the CustomItemTagManager and cache the registry lookup to use after data has been loaded.
     */
    public CustomItemTagManager(RegistryWrapper.WrapperLookup wrapperLookup) {
        super(Codecs.JSON_ELEMENT, FINDER);
        this.wrapperLookup = wrapperLookup;
    }

    /**
     *  This method will look into the thaumaturge:aspects item tag, and look for any objects that match the aspect
     *  json object, adding the correct data to the ItemAspectRegistry.
     */
    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        ItemAspectRegistry.reset();
        Optional<? extends RegistryWrapper.Impl<Aspect>> optionalRegistry = wrapperLookup.getOptional(ModRegistries.ASPECTS);
        if (optionalRegistry.isPresent()) {
            RegistryWrapper.Impl<Aspect> registry = optionalRegistry.get();
            prepared.forEach((identifier, jsonElement) -> {
                if (identifier.equals(Thaumaturge.identifier("aspects"))) {
                    if (jsonElement instanceof JsonObject jsonObject) {
                        if (JsonHelper.hasBoolean(jsonObject, "replace") && JsonHelper.getBoolean(jsonObject, "replace")) {
                            ItemAspectRegistry.reset();
                        }
                        if (JsonHelper.hasArray(jsonObject, "values")) {
                            for (JsonElement value : JsonHelper.getArray(jsonObject, "values")) {
                                if (value instanceof JsonObject jsonItemObject) {
                                    DataResult<Codecs.TagEntryId> id = Codecs.TAG_ENTRY_ID.parse(JsonOps.INSTANCE, jsonItemObject.get("id"));
                                    if(id.result().isPresent()) {
                                        Identifier itemId = id.result().get().id();
                                        if (jsonItemObject.has("aspects")) {
                                            Object2IntOpenHashMap<RegistryEntry<Aspect>> aspectAmount = new Object2IntOpenHashMap<>();
                                            JsonObject jsonAspectObject = jsonItemObject.getAsJsonObject("aspects");
                                            for (String aspectID : jsonAspectObject.asMap().keySet()) {
                                                RegistryEntry.Reference<Aspect> aspect = registry.getOrThrow(RegistryKey.of(ModRegistries.ASPECTS, Identifier.of(aspectID)));
                                                aspectAmount.put(aspect, JsonHelper.getInt(jsonAspectObject, aspectID));
                                            }
                                            ItemAspectRegistry.register(itemId, new AspectComponent(aspectAmount));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
            Thaumaturge.LOGGER.info("Finished loading aspects from item tags. Registry contains {} item aspects.", ItemAspectRegistry.size());
        }
    }

    @Override
    public Identifier getFabricId() {
        return Thaumaturge.identifier("item_aspects");
    }

    /**
     *  This method states that this should be run AFTER AspectManager.
     */
    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(Thaumaturge.identifier("aspects"));
    }

}
