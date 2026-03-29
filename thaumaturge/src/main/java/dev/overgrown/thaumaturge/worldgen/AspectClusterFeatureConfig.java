package dev.overgrown.thaumaturge.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.FeatureConfig;

public record AspectClusterFeatureConfig(BlockState state, int tries, int spreadXZ, int spreadY) implements FeatureConfig {
    public static final Codec<AspectClusterFeatureConfig> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    BlockState.CODEC.fieldOf("state").forGetter(AspectClusterFeatureConfig::state),
                    Codec.intRange(1, 128).fieldOf("tries").forGetter(AspectClusterFeatureConfig::tries),
                    Codec.intRange(0, 16).fieldOf("spread_xz").forGetter(AspectClusterFeatureConfig::spreadXZ),
                    Codec.intRange(0, 16).fieldOf("spread_y").forGetter(AspectClusterFeatureConfig::spreadY)
            ).apply(instance, AspectClusterFeatureConfig::new)
    );
}
