package dev.overgrown.thaumaturge.data_generator;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModBlockTags extends FabricTagProvider.BlockTagProvider {

    public static final TagKey<Block> BLOOK_TOOL_PICKAXE = TagKey.of(RegistryKeys.BLOCK, Identifier.of("minecraft", "mineable/pickaxe"));
    public static final TagKey<Block> BLOOK_TOOL_SHOVEL = TagKey.of(RegistryKeys.BLOCK, Identifier.of("minecraft", "mineable/shovel"));
    public static final TagKey<Block> RUBICO_ROCK_REPLACABLE = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("rubibo_rock_replacable"));
    public static final TagKey<Block> RUBICO_SAND_REPLACABLE = TagKey.of(RegistryKeys.BLOCK, Thaumaturge.identifier("rubico_sand_replacable"));

    public ModBlockTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup wrapperLookup) {
        getOrCreateTagBuilder(BLOOK_TOOL_PICKAXE)
                .add(ModBlocks.FAUCET)
                .add(ModBlocks.JAR)
                .add(ModBlocks.VESSEL)
                .add(ModBlocks.ALCHEMICAL_FURNACE)
                .add(ModBlocks.AER_CLUSTER)
                .add(ModBlocks.AQUA_CLUSTER)
                .add(ModBlocks.IGNIS_CLUSTER)
                .add(ModBlocks.TERRA_CLUSTER)
                .add(ModBlocks.ORDO_CLUSTER)
                .add(ModBlocks.PERDITIO_CLUSTER)
                .add(ModBlocks.RUBICO_ROCK);

        getOrCreateTagBuilder(BLOOK_TOOL_SHOVEL)
                .add(ModBlocks.RUBICO_SAND);

        getOrCreateTagBuilder(RUBICO_ROCK_REPLACABLE)
                .add(Blocks.STONE)
                .add(Blocks.COBBLESTONE)
                .add(Blocks.DIORITE)
                .add(Blocks.GRANITE)
                .add(Blocks.SANDSTONE)
                .add(Blocks.RED_SANDSTONE)
                .add(Blocks.DEEPSLATE)
                .add(Blocks.COBBLED_DEEPSLATE)
                .add(Blocks.NETHERRACK)
                .add(Blocks.END_STONE);

        getOrCreateTagBuilder(RUBICO_SAND_REPLACABLE)
                .add(Blocks.SAND)
                .add(Blocks.RED_SAND)
                .add(Blocks.DIRT)
                .add(Blocks.DIRT_PATH)
                .add(Blocks.COARSE_DIRT)
                .add(Blocks.FARMLAND)
                .add(Blocks.GRASS_BLOCK)
                .add(Blocks.GRAVEL)
                .add(Blocks.CLAY);
    }
}
