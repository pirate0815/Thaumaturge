package dev.overgrown.aspectslib.aspects.data;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.overgrown.aspectslib.AspectsLib;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.profiler.Profiler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This class populates the ItemAspectRegistry, with default integer values for different aspects and loads item-aspect associations from datapacks.
 * <p>
 * Responsibilities:
 * <ol type="1">
 * <li>Loads item tags from data/aspectslib/tags/items</li>
 * <li>Populates ItemAspectRegistry</li>
 * </ol>
 * </p>
 * Dependencies: Runs after AspectManager
 */
public class CustomItemTagManager extends JsonDataLoader implements IdentifiableResourceReloadListener {

    private static final Gson GSON = new Gson();

    /**
     * Instantiate the CustomItemTagManager
     */
    public CustomItemTagManager() {
        super(GSON, "tags/items");
    }

    /**
     *  This method will look into the aspectslib:aspects items tag, and look for any objects that match the aspect
     *  json object, adding the correct data to the ItemAspectRegistry.
     */
    @Override
    protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
        ItemAspectRegistry.reset();

        if (ModRegistries.ASPECTS.isEmpty()) {
            AspectsLib.LOGGER.warn("Aspects map is empty, aspects may not be loaded yet");
            return;
        }

        AspectsLib.LOGGER.info("Processing {} items tag files", prepared.size());
        prepared.forEach((identifier, jsonElement) -> {
            AspectsLib.LOGGER.debug("Processing items tag: {}", identifier);
            if (identifier.equals(AspectsLib.identifier("aspects"))) {
                AspectsLib.LOGGER.info("Found aspects tag file, processing...");
                if (jsonElement instanceof JsonObject jsonObject) {
                    if (JsonHelper.hasBoolean(jsonObject, "replace") && JsonHelper.getBoolean(jsonObject, "replace")) {
                        ItemAspectRegistry.reset();
                    }
                    if (JsonHelper.hasArray(jsonObject, "values")) {
                        JsonArray values = JsonHelper.getArray(jsonObject, "values");
                        AspectsLib.LOGGER.info("Processing {} aspect entries", values.size());
                        for (JsonElement value : values) {
                            if (value instanceof JsonObject jsonItemObject) {
                                DataResult<Codecs.TagEntryId> id = Codecs.TAG_ENTRY_ID.parse(JsonOps.INSTANCE, jsonItemObject.get("id"));
                                if(id.result().isPresent()) {
                                    Codecs.TagEntryId tagEntry = id.result().get();
                                    Identifier itemId = tagEntry.id();
                                    AspectsLib.LOGGER.debug("Processing entry: {} (tag: {})", itemId, tagEntry.tag());

                                    
                                    if (jsonItemObject.has("aspects")) {
                                        Object2IntOpenHashMap<Identifier> aspectAmount = new Object2IntOpenHashMap<>();
                                        JsonObject jsonAspectObject = jsonItemObject.getAsJsonObject("aspects");
                                        for (String aspectID : jsonAspectObject.asMap().keySet()) {
                                            Identifier aspectIdentifier = new Identifier(aspectID);
                                            if (ModRegistries.ASPECTS.containsKey(aspectIdentifier)) {
                                                int amount = JsonHelper.getInt(jsonAspectObject, aspectID);
                                                aspectAmount.put(aspectIdentifier, amount);
                                                AspectsLib.LOGGER.debug("Added aspect {} with amount {} to items {}", aspectID, amount, itemId);
                                            } else {
                                                AspectsLib.LOGGER.warn("Could not find aspect: {}", aspectID);
                                            }
                                        }
                                        
                                        if (!aspectAmount.isEmpty()) {
                                            ItemAspectRegistry.register(itemId, new AspectData(aspectAmount));
                                            AspectsLib.LOGGER.debug("Registered {} aspects for items: {}", aspectAmount.size(), itemId);
                                        }
                                    }
                                } else {
                                    AspectsLib.LOGGER.warn("Failed to parse items ID from: {}", jsonItemObject.get("id"));
                                }
                            }
                        }
                    }
                } else {
                    AspectsLib.LOGGER.warn("aspects.json is not a valid JSON object");
                }
            }
        });
        AspectsLib.LOGGER.info("Finished loading aspects from items tags. Registry contains {} items aspects.", ItemAspectRegistry.size());
    }

    @Override
    public Identifier getFabricId() {
        return AspectsLib.identifier("item_aspects");
    }

    /**
     *  This method states that this should be run AFTER AspectManager.
     */
    @Override
    public Collection<Identifier> getFabricDependencies() {
        return List.of(AspectsLib.identifier("aspects"));
    }
}