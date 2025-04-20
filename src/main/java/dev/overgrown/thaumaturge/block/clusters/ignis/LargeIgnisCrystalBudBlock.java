package dev.overgrown.thaumaturge.block.clusters.ignis;

import net.minecraft.block.AbstractBlock;

public class LargeIgnisCrystalBudBlock extends AbstractIgnisCrystalBlock {
    public LargeIgnisCrystalBudBlock(AbstractBlock.Settings settings) {
        super(5.0f, 5.0f, settings);
    }

    @Override
    protected int getParticleCount() {
        return 3;
    }
}