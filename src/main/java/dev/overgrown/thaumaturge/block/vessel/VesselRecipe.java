package dev.overgrown.thaumaturge.block.vessel;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.*;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeBookCategories;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;
import java.util.ArrayList;

public record VesselRecipe(
        Identifier id,
        List<Item> sequence,
        VesselBlock.FluidType fluidType,
        int level,
        ItemStack output
) implements Recipe<RecipeInput> {

    public RecipeBookCategory getRecipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public IngredientPlacement getIngredientPlacement() {
        return IngredientPlacement.NONE;
    }

    @Override
    public boolean matches(RecipeInput input, World world) {
        return false;
    }

    @Override
    public ItemStack craft(RecipeInput input, RegistryWrapper.WrapperLookup registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<VesselRecipe> {
        public static final Type INSTANCE = new Type();
        public static final Identifier ID = Identifier.of("thaumaturge", "vessel_recipe");
    }

    public static class Serializer implements RecipeSerializer<VesselRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final Identifier ID = Type.ID;

        public static final MapCodec<VesselRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(
                        Identifier.CODEC.fieldOf("id").forGetter(VesselRecipe::id),
                        Codec.list(Registries.ITEM.getCodec()).fieldOf("sequence").forGetter(VesselRecipe::sequence),
                        VesselBlock.FluidType.CODEC.fieldOf("fluidType").forGetter(VesselRecipe::fluidType),
                        Codec.INT.fieldOf("level").forGetter(VesselRecipe::level),
                        ItemStack.CODEC.fieldOf("output").forGetter(VesselRecipe::output)
                ).apply(instance, VesselRecipe::new)
        );

        public static final PacketCodec<RegistryByteBuf, VesselRecipe> PACKET_CODEC = PacketCodec.tuple(
                Identifier.PACKET_CODEC, VesselRecipe::id,
                PacketCodecs.collection(ArrayList::new, PacketCodecs.registryValue(Registries.ITEM.getKey())),
                VesselRecipe::sequence,
                PacketCodecs.codec(VesselBlock.FluidType.CODEC), VesselRecipe::fluidType,
                PacketCodecs.VAR_INT, VesselRecipe::level,
                ItemStack.PACKET_CODEC, VesselRecipe::output,
                VesselRecipe::new
        );

        @Override
        public MapCodec<VesselRecipe> codec() {
            return CODEC;
        }

        @Override
        public PacketCodec<RegistryByteBuf, VesselRecipe> packetCodec() {
            return PACKET_CODEC;
        }
    }
}