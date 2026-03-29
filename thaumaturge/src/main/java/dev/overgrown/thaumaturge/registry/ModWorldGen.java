package dev.overgrown.thaumaturge.registry;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.worldgen.AspectClusterFeature;
import dev.overgrown.thaumaturge.worldgen.AspectClusterFeatureConfig;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

public class ModWorldGen {

    // Custom feature type
    public static final Feature<AspectClusterFeatureConfig> ASPECT_CLUSTER_FEATURE =
            Registry.register(Registries.FEATURE, Thaumaturge.identifier("aspect_cluster"),
                    new AspectClusterFeature(AspectClusterFeatureConfig.CODEC));

    // Placed feature keys (reference JSON files in data/thaumaturge/worldgen/placed_feature/)
    private static final RegistryKey<PlacedFeature> IGNIS_CLUSTER =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Thaumaturge.identifier("ignis_cluster"));
    private static final RegistryKey<PlacedFeature> IGNIS_CLUSTER_NETHER =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Thaumaturge.identifier("ignis_cluster_nether"));
    private static final RegistryKey<PlacedFeature> AER_CLUSTER =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Thaumaturge.identifier("aer_cluster"));
    private static final RegistryKey<PlacedFeature> AQUA_CLUSTER =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Thaumaturge.identifier("aqua_cluster"));
    private static final RegistryKey<PlacedFeature> TERRA_CLUSTER =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Thaumaturge.identifier("terra_cluster"));
    private static final RegistryKey<PlacedFeature> ORDO_CLUSTER =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Thaumaturge.identifier("ordo_cluster"));
    private static final RegistryKey<PlacedFeature> PERDITIO_CLUSTER =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Thaumaturge.identifier("perditio_cluster"));
    private static final RegistryKey<PlacedFeature> PERDITIO_CLUSTER_END =
            RegistryKey.of(RegistryKeys.PLACED_FEATURE, Thaumaturge.identifier("perditio_cluster_end"));

    public static void initialize() {
        // Ignis - Overworld: near lava level
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                IGNIS_CLUSTER
        );

        // Ignis - Nether: all nether biomes
        BiomeModifications.addFeature(
                BiomeSelectors.foundInTheNether(),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                IGNIS_CLUSTER_NETHER
        );

        // Aer - Mountain biomes at high Y
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_MOUNTAIN),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                AER_CLUSTER
        );

        // Aqua - Ocean biomes (all ocean types)
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_OCEAN)
                        .or(BiomeSelectors.tag(BiomeTags.IS_DEEP_OCEAN)),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                AQUA_CLUSTER
        );

        // Terra - Jungle biomes and Dripstone Caves
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_JUNGLE)
                        .or(BiomeSelectors.includeByKey(BiomeKeys.DRIPSTONE_CAVES)),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                TERRA_CLUSTER
        );

        // Ordo - All overworld caves (rare)
        BiomeModifications.addFeature(
                BiomeSelectors.foundInOverworld(),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                ORDO_CLUSTER
        );

        // Perditio - Badlands and Mushroom Fields
        BiomeModifications.addFeature(
                BiomeSelectors.tag(BiomeTags.IS_BADLANDS)
                        .or(BiomeSelectors.includeByKey(BiomeKeys.MUSHROOM_FIELDS)),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                PERDITIO_CLUSTER
        );

        // Perditio - End Islands
        BiomeModifications.addFeature(
                BiomeSelectors.foundInTheEnd(),
                GenerationStep.Feature.UNDERGROUND_DECORATION,
                PERDITIO_CLUSTER_END
        );
    }
}
