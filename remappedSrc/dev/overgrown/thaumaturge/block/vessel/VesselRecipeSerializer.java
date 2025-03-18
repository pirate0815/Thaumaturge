package dev.overgrown.thaumaturge.block.vessel;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.RecipeSerializer;

public class VesselRecipeSerializer implements RecipeSerializer<VesselRecipe> {
    public static final VesselRecipeSerializer INSTANCE = new VesselRecipeSerializer();

    @Override
    public MapCodec<VesselRecipe> codec() {
        return VesselRecipe.Serializer.CODEC;
    }

    @Override
    public PacketCodec<RegistryByteBuf, VesselRecipe> packetCodec() {
        return VesselRecipe.Serializer.PACKET_CODEC;
    }
}