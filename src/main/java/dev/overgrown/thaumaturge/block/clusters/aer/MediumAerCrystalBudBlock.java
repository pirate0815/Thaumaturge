package dev.overgrown.thaumaturge.block.clusters.aer;

public class MediumAerCrystalBudBlock extends AbstractAerCrystalBlock {
    public MediumAerCrystalBudBlock(Settings settings) {
        super(4.0f, 4.0f, settings);
    }

    @Override
    protected int getParticleCount() {
        return 2;
    }

    @Override
    protected float getPushStrength() {
        return 0.7f;
    }
}