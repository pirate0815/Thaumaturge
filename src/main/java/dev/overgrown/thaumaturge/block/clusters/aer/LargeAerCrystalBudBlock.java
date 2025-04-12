package dev.overgrown.thaumaturge.block.clusters.aer;

public class LargeAerCrystalBudBlock extends AbstractAerCrystalBlock {
    public LargeAerCrystalBudBlock(Settings settings) {
        super(5.0f, 5.0f, settings);
    }

    @Override protected int getParticleCount() {
        return 3;
    }
    @Override protected float getPushStrength() {
        return 0.9f;
    }
}