package dev.overgrown.thaumaturge.spell.focal;

/**
 * Defines a tunable parameter for a spell component (e.g. damage, burn duration).
 *
 * @param key                unique key used in NBT and parameter value maps
 * @param displayName        human-readable label shown in the focal manipulator UI
 * @param min                minimum allowed value (inclusive)
 * @param max                maximum allowed value (inclusive)
 * @param defaultValue       the starting value; complexity cost is zero at this value
 * @param complexityCostPerUnit additional complexity per unit above the default
 */
public record ParameterDef(
        String key,
        String displayName,
        float min,
        float max,
        float defaultValue,
        float complexityCostPerUnit
) {}
