package dev.overgrown.aspectslib.spell.resonance;

import dev.overgrown.aspectslib.AspectsLib;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Global registry for all {@link VolatileResonancePair}s.
 *
 * <p>All six canonical pairs from the Codex are pre-registered.  Mods that add
 * custom Aspects may register additional pairs during {@code onInitialize}.
 *
 * <h3>Usage in the spell pipeline</h3>
 * <ol>
 *   <li>Before activating a spell, call {@link #detectPairs} with the spell's
 *       Aspect set to discover all ⚡ pairs present.</li>
 *   <li>For each detected pair, decide whether it is:
 *       <ul>
 *         <li><b>Managed</b> (⊗ operator present) — pay the opposition cost but
 *             prevent byproduct generation.</li>
 *         <li><b>Unmanaged</b> — generate the byproduct Aspect and apply the
 *             stability penalty via {@link #applyVolatileByproduct}.</li>
 *       </ul>
 *   </li>
 * </ol>
 */
public final class VolatileResonanceRegistry {

    private static final List<VolatileResonancePair> PAIRS = new ArrayList<>();

    static {
        // ── Canonical six pairs ──────────────────────────────────────────────
        register(VolatileResonancePair.of(
                id("aqua"), id("ignis"),
                id("aer"), // AER burst; POT discharge handled separately
                1.5,
                "Aqua+Ignis: steam burst + Potentia discharge. Archetypal volatile pair."
        ));
        register(VolatileResonancePair.of(
                id("ordo"), id("perditio"),
                id("permutatio"),
                2.0,
                "Ordo+Perditio: forced Permutatio surge. Mechanism behind Vessel explosions."
        ));
        register(VolatileResonancePair.of(
                id("gelum"), id("ignis"),
                id("aqua"), // water + AER burst
                1.2,
                "Gelum+Ignis: abrupt phase transition; faster/smaller than Aqua+Ignis."
        ));
        register(VolatileResonancePair.asymmetric(
                id("tenebrae"), id("lux"),
                id("vacuos"),
                1.3,
                "Lux+Tenebrae: Tenebrae consumes Lux asymmetrically (~2:3), leaving VAC residue."
        ));
        register(VolatileResonancePair.of(
                id("vitium"), id("victus"),
                id("exanimis"),
                1.8,
                "Vitium+Victus: corruption meets life, generates spreading Exanimis pulse."
        ));
        register(VolatileResonancePair.of(
                id("vacuos"), id("praecantatio"),
                id("alienis"),
                2.5,
                "Vacuos+Praecantatio: void consumes self-referential magic. Alienis residue is self-propagating."
        ));
    }

    private VolatileResonanceRegistry() {}

    private static Identifier id(String path) {
        return AspectsLib.identifier(path);
    }

    /**
     * Registers a new {@link VolatileResonancePair}.
     * Pre-existing pairs for the same Aspect combination are NOT replaced;
     * new registrations append to the list.
     */
    public static void register(VolatileResonancePair pair) {
        PAIRS.add(pair);
        AspectsLib.LOGGER.debug("Registered volatile pair: {} ⚡ {}", pair.aspect1(), pair.aspect2());
    }

    /**
     * Returns the {@link VolatileResonancePair} for the given two Aspects, or
     * {@link Optional#empty()} if none is registered.
     */
    public static Optional<VolatileResonancePair> get(Identifier a, Identifier b) {
        for (VolatileResonancePair p : PAIRS) {
            if (p.matches(a, b)) return Optional.of(p);
        }
        return Optional.empty();
    }

    /**
     * Returns {@code true} if the two Aspects form a registered Volatile pair.
     */
    public static boolean isVolatile(Identifier a, Identifier b) {
        return get(a, b).isPresent();
    }

    /**
     * Scans a collection of Aspect IDs and returns all Volatile pairs present.
     * Used during spell validation and casting to detect unmanaged ⚡ pairs.
     *
     * @param aspectIds the full set of Aspects in the spell or local environment
     * @return list of all Volatile pairs who's both members appear in the set
     */
    public static List<VolatileResonancePair> detectPairs(Collection<Identifier> aspectIds) {
        List<Identifier> ids = new ArrayList<>(aspectIds);
        List<VolatileResonancePair> found = new ArrayList<>();

        for (int i = 0; i < ids.size(); i++) {
            for (int j = i + 1; j < ids.size(); j++) {
                get(ids.get(i), ids.get(j)).ifPresent(found::add);
            }
        }
        return found;
    }

    /** Returns an unmodifiable view of all registered pairs. */
    public static List<VolatileResonancePair> all() {
        return Collections.unmodifiableList(PAIRS);
    }

    /**
     * Applies a Volatile byproduct to the spell context when a pair fires
     * without the {@code ⊗} managed-opposition operator.
     *
     * <p>Concrete effects (particle bursts, block changes, entity damage) are
     * left to consuming mods via the {@link dev.overgrown.aspectslib.spell.effect.AspectEffectRegistry}.
     * This method records the event in the context's data map so modifiers and
     * executing spells can query it.
     *
     * @param ctx   the active spell context
     * @param pair  the pair that fired
     * @param i1    intensity of aspect1 in the spell
     * @param i2    intensity of aspect2 in the spell
     */
    public static void applyVolatileByproduct(
            dev.overgrown.aspectslib.spell.SpellContext ctx,
            VolatileResonancePair pair, int i1, int i2) {

        int byproductIntensity = pair.computeByproductIntensity(i1, i2);
        pair.getByproductAspect().ifPresent(byproductId -> {
            // Store for downstream consumers
            String key = "volatile_byproduct:" + pair.aspect1() + "+" + pair.aspect2();
            ctx.putData(key, new ByproductResult(byproductId, byproductIntensity));

            // Reduce spell stability — each unmanaged Volatile pair costs 15 % stability
            double currentStability = ctx.getMetadata().getStability();
            ctx.getMetadata().set(
                    dev.overgrown.aspectslib.spell.SpellMetadata.STABILITY,
                    Math.max(0.0, currentStability - 0.15)
            );

            AspectsLib.LOGGER.debug(
                    "Volatile pair {}/{} fired: byproduct {} × {}, stability now {}",
                    pair.aspect1(), pair.aspect2(),
                    byproductId, byproductIntensity,
                    ctx.getMetadata().getStability()
            );
        });
    }

    /**
     * Result of a Volatile byproduct stored in {@link dev.overgrown.aspectslib.spell.SpellContext}.
     */
    public record ByproductResult(Identifier aspectId, int intensity) {}
}