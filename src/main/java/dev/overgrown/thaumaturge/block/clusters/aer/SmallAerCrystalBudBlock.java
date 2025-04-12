package dev.overgrown.thaumaturge.block.clusters.aer;

public class SmallAerCrystalBudBlock extends AbstractAerCrystalBlock {
    public SmallAerCrystalBudBlock(Settings settings) {
        super(3.0f, 3.0f, settings);
    }

    @Override protected int getParticleCount() {
        return 1;
    }
    @Override protected float getPushStrength() {
        return 0.5f;
    }
}