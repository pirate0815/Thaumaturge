package dev.overgrown.thaumaturge.block.clusters.ignis;

import net.minecraft.block.AbstractBlock;

public class SmallIgnisCrystalBudBlock extends AbstractIgnisCrystalBlock {
    public SmallIgnisCrystalBudBlock(AbstractBlock.Settings settings) {
        super(3.0f, 3.0f, settings);
    }

    @Override
    protected int getParticleCount() {
        return 1;
    }
}