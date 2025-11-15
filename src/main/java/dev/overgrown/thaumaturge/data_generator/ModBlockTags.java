package dev.overgrown.thaumaturge.data_generator;

import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTags extends FabricTagProvider.BlockTagProvider {

    public static final TagKey<Block> BLOOK_TOOL_PICKAXE = TagKey.of(RegistryKeys.BLOCK, Identifier.of("minecraft", "mineable/pickaxe"));

    public ModBlockTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BLOOK_TOOL_PICKAXE)
                .add(ModBlocks.FAUCET)
                .add(ModBlocks.JAR)
                .add(ModBlocks.VESSEL)
                .add(ModBlocks.ALCHEMICAL_FURNACE);
    }
}
