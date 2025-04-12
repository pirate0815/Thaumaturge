package dev.overgrown.thaumaturge.block.clusters.aer;

import net.minecraft.util.math.intprovider.UniformIntProvider;

public class AerCrystalClusterBlock extends AbstractAerCrystalBlock {
    public AerCrystalClusterBlock(UniformIntProvider uniformIntProvider, Settings settings) {
        super(7.0f, 7.0f, settings);
    }

    @Override
    protected int getParticleCount() {
        return 4;
    }
    @Override
    protected float getPushStrength() {
        return 1.0f;
    }
}