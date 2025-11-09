package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.data_generator.BlockLootTableProvider;
import dev.overgrown.thaumaturge.data_generator.BlockTagProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class ThaumaturgeDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(BlockLootTableProvider::new);
        pack.addProvider(BlockTagProvider::new);
	}
}
