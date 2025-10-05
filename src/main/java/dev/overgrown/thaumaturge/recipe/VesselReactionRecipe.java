package dev.overgrown.thaumaturge.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.entity.VesselBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class VesselReactionRecipe implements Recipe<Inventory> {

    private final Identifier id;
    private final Map<String, Integer> aspectsIn;
    private final Map<String, Integer> aspectsOut;
    private final boolean[] activeAt;
    private final double deltaT;


    public VesselReactionRecipe(Identifier id, Map<String, Integer> aspects_in, Map<String, Integer> aspects_out, boolean[] active_at, double delta_t) {
        this.id = id;
        this.aspectsIn = aspects_in;
        this.aspectsOut = aspects_out;
        this.activeAt = active_at;
        this.deltaT = delta_t;
    }

    public Map<String, Integer> getAspectsIn() {return aspectsIn;}

    public Map<String, Integer> getAspectsOut() {return aspectsOut;}

    public boolean activeAt(VesselBlockEntity.TemperatureRange temperatureRange) {
        return activeAt[temperatureRange.getValue()];
    }

    public boolean[] getActiveAt() {return activeAt;}

    public double getDeltaT() {return deltaT;}

    @Override
    public boolean matches(Inventory inventory, World world) {return false;}

    @Override
    public ItemStack craft(Inventory inventory, DynamicRegistryManager registryManager) {return ItemStack.EMPTY;}

    @Override
    public boolean fits(int width, int height) {return false;}

    @Override
    public ItemStack getOutput(DynamicRegistryManager registryManager) {return ItemStack.EMPTY;}

    @Override
    public RecipeSerializer<?> getSerializer() {return Serializer.INSTANCE;}

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<VesselReactionRecipe> {
        public Type() {}
        public static final Type INSTANCE = new Type();
    }

    @Override
    public Identifier getId() {return id;}

    public static class Serializer implements RecipeSerializer<VesselReactionRecipe> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public VesselReactionRecipe read(Identifier id, JsonObject json) {
            Map<String, Integer> aspectsIn = new HashMap<>();
            JsonObject aspectsInJson = JsonHelper.getObject(json, "aspects_in");
            aspectsInJson.entrySet().forEach(entry ->
                    aspectsIn.put(entry.getKey(), entry.getValue().getAsInt()));

            Map<String, Integer> aspectsOut = new HashMap<>();
            JsonObject aspectsOutJson = JsonHelper.getObject(json, "aspects_out");
            aspectsOutJson.entrySet().forEach(entry ->
                    aspectsOut.put(entry.getKey(), entry.getValue().getAsInt()));

            boolean[] activeAt = new boolean[5];
            JsonArray activeAtJson = JsonHelper.getArray(json, "active_at");
            activeAtJson.forEach(jsonElement -> {
                switch (jsonElement.getAsString()) {
                    case "lukewarm":
                        activeAt[VesselBlockEntity.TemperatureRange.LUKEWARM.getValue()] = true;
                        break;
                    case "warm":
                        activeAt[VesselBlockEntity.TemperatureRange.WARM.getValue()] = true;
                        break;
                    case "hot":
                        activeAt[VesselBlockEntity.TemperatureRange.HOT.getValue()] = true;
                        break;
                    case "scolding":
                        activeAt[VesselBlockEntity.TemperatureRange.SCOLDING.getValue()] = true;
                        break;
                    case "boiling":
                        activeAt[VesselBlockEntity.TemperatureRange.BOILING.getValue()] = true;
                        break;
                    default:
                        Thaumaturge.LOGGER.warn("Unknown reaction temperature range {} used in {}", jsonElement.getAsString(), id);
                        break;
                }
            });
            double deltaT = JsonHelper.getDouble(json, "delta_t");
            return new VesselReactionRecipe(id, aspectsIn, aspectsOut, activeAt, deltaT);
        }

        @Override
        public void write(PacketByteBuf buf, VesselReactionRecipe recipe) {
            buf.writeInt(recipe.aspectsIn.size());
            for (Map.Entry<String, Integer> entry : recipe.aspectsIn.entrySet()) {
                buf.writeString(entry.getKey());
                buf.writeVarInt(entry.getValue());
            }
            buf.writeInt(recipe.aspectsOut.size());
            for (Map.Entry<String, Integer> entry : recipe.aspectsOut.entrySet()) {
                buf.writeString(entry.getKey());
                buf.writeVarInt(entry.getValue());
            }
            for (int i = 0; i < 5; i++) {
                buf.writeBoolean(recipe.getActiveAt()[i]);
            }
            buf.writeDouble(recipe.deltaT);
        }

        @Override
        public VesselReactionRecipe read(Identifier id, PacketByteBuf buf) {

            int aspectsInSize = buf.readInt();
            Map<String, Integer> aspectsIn = new HashMap<>(aspectsInSize);
            for (int i = 0; i < aspectsInSize; i++) {
                aspectsIn.put(buf.readString(), buf.readVarInt());
            }

            int aspectsOutSize = buf.readInt();
            Map<String, Integer> aspectsOut = new HashMap<>(aspectsOutSize);
            for (int i = 0; i < aspectsOutSize; i++) {
                aspectsOut.put(buf.readString(), buf.readVarInt());
            }

            boolean[] activeAt = new boolean[5];
            for (int i = 0; i < 5; i++) {
                activeAt[i] = buf.readBoolean();
            }

            double deltaT = buf.readDouble();

            return new VesselReactionRecipe(id, aspectsIn, aspectsOut, activeAt, deltaT);
        }


    }
}
