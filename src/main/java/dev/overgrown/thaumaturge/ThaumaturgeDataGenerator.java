package dev.overgrown.thaumaturge;

import com.klikli_dev.modonomicon.datagen.ItemTagsProvider;
import dev.overgrown.thaumaturge.data_generator.ModBlockLootTables;
import dev.overgrown.thaumaturge.data_generator.ModBlockTags;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ThaumaturgeDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModBlockLootTables::new);
        pack.addProvider(ModBlockTags::new);
        pack.addProvider(ItemTagsProvider::new);
	}
}
