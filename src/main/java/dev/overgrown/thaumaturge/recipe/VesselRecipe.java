package dev.overgrown.thaumaturge.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.overgrown.thaumaturge.block.entity.VesselBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class VesselRecipe implements Recipe<Inventory> {
    private final Identifier id;
    private final Map<String, Integer> aspects;
    private final ItemStack catalyst;
    private final boolean consumesCatalyst;
    private final ItemStack output;

    public VesselRecipe(Identifier id, Map<String, Integer> aspects, ItemStack catalyst, boolean consumesCatalyst, ItemStack output) {
        this.id = id;
        this.aspects = aspects;
        this.catalyst = catalyst;
        this.consumesCatalyst = consumesCatalyst;
        this.output = output;
    }

    public Map<String, Integer> getAspects() {
        return aspects;
    }

    public ItemStack getCatalyst() {
        return catalyst;
    }

    public boolean consumesCatalyst() {
        return consumesCatalyst;
    }

    @Override
    public boolean matches(Inventory inventory, World world) {
        return false;
    }

    @Override
    public DefaultedList<Ingredient> getIngredients() {
        // Return actual ingredients if needed
        return DefaultedList.of();
    }

    public boolean matches(VesselBlockEntity vessel) {
        // Check if catalyst matches (if recipe requires one)
        if (!catalyst.isEmpty()) {
            ItemStack vesselCatalyst = vessel.getCatalyst();
            if (vesselCatalyst.isEmpty() || !ItemStack.areItemsEqual(catalyst, vesselCatalyst)) {
                return false;
            }
        }

        // Check if aspects are sufficient
        return aspects.entrySet().stream()
                .allMatch(entry -> vessel.getAspects().getOrDefault(entry.getKey(), 0) >= entry.getValue());
    }

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {
        return output.copy();
    }

    @Override
    public boolean fits(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {
        return output;
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<VesselRecipe> {
        public Type() {}
        public static final Type INSTANCE = new Type();
    }

    public static class Serializer implements RecipeSerializer<VesselRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public VesselRecipe read(Identifier id, JsonObject json) {
            Map<String, Integer> aspects = new HashMap<>();
            JsonObject aspectsJson = JsonHelper.getObject(json, "aspects");
            aspectsJson.entrySet().forEach(entry ->
                    aspects.put(entry.getKey(), entry.getValue().getAsInt()));

            ItemStack catalyst = ItemStack.EMPTY;
            if (json.has("catalyst")) {
                catalyst = new ItemStack(JsonHelper.getItem(json, "catalyst"));
            }

            boolean consumesCatalyst = JsonHelper.getBoolean(json, "consumes_catalyst", false);

            // Handle both string and object output formats
            ItemStack output;
            JsonElement outputElement = json.get("output");
            if (outputElement.isJsonObject()) {
                JsonObject outputObj = outputElement.getAsJsonObject();
                Item item = JsonHelper.getItem(outputObj, "item");
                int count = JsonHelper.getInt(outputObj, "count", 1);
                output = new ItemStack(item, count);
            } else {
                output = new ItemStack(JsonHelper.getItem(json, "output"));
            }

            return new VesselRecipe(id, aspects, catalyst, consumesCatalyst, output);
        }

        @Override
        public VesselRecipe read(Identifier id, PacketByteBuf buf) {
            Map<String, Integer> aspects = new HashMap<>();
            int aspectCount = buf.readVarInt();
            for (int i = 0; i < aspectCount; i++) {
                aspects.put(buf.readString(), buf.readVarInt());
            }

            ItemStack catalyst = buf.readItemStack();
            boolean consumesCatalyst = buf.readBoolean();
            ItemStack output = buf.readItemStack();

            return new VesselRecipe(id, aspects, catalyst, consumesCatalyst, output);
        }

        @Override
        public void write(PacketByteBuf buf, VesselRecipe recipe) {
            buf.writeVarInt(recipe.aspects.size());
            for (Map.Entry<String, Integer> entry : recipe.aspects.entrySet()) {
                buf.writeString(entry.getKey());
                buf.writeVarInt(entry.getValue());
            }

            buf.writeItemStack(recipe.catalyst);
            buf.writeBoolean(recipe.consumesCatalyst);
            buf.writeItemStack(recipe.output);
        }
    }
}