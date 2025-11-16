package dev.overgrown.thaumaturge.data_generator;

import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;

public class ModBlockLootTables extends FabricBlockLootTableProvider {



    public ModBlockLootTables(FabricDataOutput dataOutput) {
        super(dataOutput);
    }

    @Override
    public void generate() {
        addDrop(ModBlocks.FAUCET);
        addDrop(ModBlocks.JAR);
        addDrop(ModBlocks.VESSEL);
        addDrop(ModBlocks.ALCHEMICAL_FURNACE);
    }
}
