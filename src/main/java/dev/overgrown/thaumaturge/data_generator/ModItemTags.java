package dev.overgrown.thaumaturge.data_generator;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTags extends FabricTagProvider.ItemTagProvider {

    public static final TagKey<Item> VESSEL_NO_SLUDGE = TagKey.of(RegistryKeys.ITEM, Thaumaturge.identifier("vessel_no_sludge"));

    public ModItemTags(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture, @Nullable BlockTagProvider blockTagProvider) {
        super(output, completableFuture, blockTagProvider);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {
        getOrCreateTagBuilder(VESSEL_NO_SLUDGE).add(AspectsLib.identifier("aer_aspect_shard"));
    }
}
