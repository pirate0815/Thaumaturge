package dev.overgrown.thaumaturge.block.clusters.ignis;

import net.minecraft.util.math.intprovider.UniformIntProvider;

public class IgnisCrystalClusterBlock extends AbstractIgnisCrystalBlock {
    public IgnisCrystalClusterBlock(UniformIntProvider uniformIntProvider, Settings settings) {
        super(7.0f, 7.0f, settings);
    }

    @Override
    protected int getParticleCount() {
        return 4;
    }
}