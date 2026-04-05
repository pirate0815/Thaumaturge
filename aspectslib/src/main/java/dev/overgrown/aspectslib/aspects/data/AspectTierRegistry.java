package dev.overgrown.aspectslib.aspects.data;

import dev.overgrown.aspectslib.AspectsLib;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps every Aspect {@link Identifier} to its {@link AspectTier}.
 *
 * <p>Tier determines the per-intensity Aether cost used in
 * {@link dev.overgrown.aspectslib.spell.cost.AetherCostCalculator}:
 * <ul>
 *   <li>Tier I  (Primal): ×5 Aether per intensity point</li>
 *   <li>Tier II (Secondary): ×8 Aether per intensity point</li>
 *   <li>Tier III (Tertiary): ×12 Aether per intensity point</li>
 *   <li>Tier IV (Quaternary): ×20 Aether per intensity point</li>
 * </ul>
 *
 * <p>All 38 canonical Aspects are pre-registered using the {@code aspectslib}
 * namespace. Mods that add custom Aspects should call {@link #register(Identifier, AspectTier)} during their {@code onInitialize}.
 */
public final class AspectTierRegistry {

    private static final Map<Identifier, AspectTier> REGISTRY = new HashMap<>();

    static {
        // Tier I: Primal
        register("aer", AspectTier.PRIMAL);
        register("aqua", AspectTier.PRIMAL);
        register("terra", AspectTier.PRIMAL);
        register("ignis", AspectTier.PRIMAL);
        register("ordo", AspectTier.PRIMAL);
        register("perditio", AspectTier.PRIMAL);

        // Tier II: Secondary
        register("gelum", AspectTier.SECONDARY);
        register("lux", AspectTier.SECONDARY);
        register("metallum", AspectTier.SECONDARY);
        register("mortuus", AspectTier.SECONDARY);
        register("motus", AspectTier.SECONDARY);
        register("permutatio", AspectTier.SECONDARY);
        register("potentia", AspectTier.SECONDARY);
        register("vacuos", AspectTier.SECONDARY);
        register("victus", AspectTier.SECONDARY);
        register("vitreus", AspectTier.SECONDARY);

        // Tier III: Tertiary
        register("bestia", AspectTier.TERTIARY);
        register("exanimis", AspectTier.TERTIARY);
        register("fames", AspectTier.TERTIARY);
        register("herba", AspectTier.TERTIARY);
        register("instrumentum", AspectTier.TERTIARY);
        register("praecantatio", AspectTier.TERTIARY);
        register("spiritus", AspectTier.TERTIARY);
        register("tenebrae", AspectTier.TERTIARY);
        register("vinculum", AspectTier.TERTIARY);
        register("volatus", AspectTier.TERTIARY);

        // Tier IV: Quaternary
        register("alienis", AspectTier.QUATERNARY);
        register("alkimia", AspectTier.QUATERNARY);
        register("auram", AspectTier.QUATERNARY);
        register("aversio", AspectTier.QUATERNARY);
        register("cognitio", AspectTier.QUATERNARY);
        register("desiderium", AspectTier.QUATERNARY);
        register("fabrico", AspectTier.QUATERNARY);
        register("humanus", AspectTier.QUATERNARY);
        register("machina", AspectTier.QUATERNARY);
        register("praemunio", AspectTier.QUATERNARY);
        register("sensus", AspectTier.QUATERNARY);
        register("vitium", AspectTier.QUATERNARY);
    }

    private AspectTierRegistry() {}

    // Registration
    private static void register(String path, AspectTier tier) {
        REGISTRY.put(AspectsLib.identifier(path), tier);
    }

    /**
     * Registers a custom Aspect's tier.  Overwrites any previous registration
     * for the same {@link Identifier}.
     */
    public static void register(Identifier aspectId, AspectTier tier) {
        REGISTRY.put(aspectId, tier);
    }

    // Lookup
    /**
     * Returns the {@link AspectTier} for {@code aspectId}, or
     * {@link AspectTier#PRIMAL} as a safe default when the id is unknown.
     */
    public static AspectTier getTier(Identifier aspectId) {
        return REGISTRY.getOrDefault(aspectId, AspectTier.PRIMAL);
    }

    /** Returns an unmodifiable view of all registrations. */
    public static Map<Identifier, AspectTier> all() {
        return Collections.unmodifiableMap(REGISTRY);
    }
}