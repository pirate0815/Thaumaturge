package dev.overgrown.thaumaturge.block.clusters.ignis;

import net.minecraft.block.AbstractBlock;

public class MediumIgnisCrystalBudBlock extends AbstractIgnisCrystalBlock {
    public MediumIgnisCrystalBudBlock(AbstractBlock.Settings settings) {
        super(4.0f, 4.0f, settings);
    }

    @Override
    protected int getParticleCount() {
        return 2;
    }
}