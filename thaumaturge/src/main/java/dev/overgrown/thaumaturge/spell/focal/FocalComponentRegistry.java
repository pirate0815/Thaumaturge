package dev.overgrown.thaumaturge.spell.focal;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.util.Identifier;

import java.util.*;

/**
 * Registry of all spell components available in the Focal Manipulator.
 * Components are divided into three categories: mediums, effects, and modifiers.
 */
public final class FocalComponentRegistry {

    private static final Map<Identifier, SpellComponentDefinition> REGISTRY = new LinkedHashMap<>();

    private FocalComponentRegistry() {}

    public static void register(SpellComponentDefinition def) {
        Objects.requireNonNull(def, "definition must not be null");
        if (REGISTRY.containsKey(def.id())) {
            throw new IllegalArgumentException("Component already registered: " + def.id());
        }
        REGISTRY.put(def.id(), def);
    }

    public static SpellComponentDefinition get(Identifier id) {
        return REGISTRY.get(id);
    }

    public static Collection<SpellComponentDefinition> all() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static List<SpellComponentDefinition> byType(SpellComponentType type) {
        List<SpellComponentDefinition> result = new ArrayList<>();
        for (SpellComponentDefinition def : REGISTRY.values()) {
            if (def.type() == type) result.add(def);
        }
        return result;
    }

    /** Root definition (always present at top of spell tree) */
    public static final Identifier ROOT_ID = Thaumaturge.identifier("root");

    /** Common socket sets */
    private static final Set<Socket> ROOT_PROVIDES = Set.of(Socket.TARGET, Socket.TRAJECTORY);
    private static final Set<Socket> ROOT_REQUIRES = Set.of();

    private static final Set<Socket> MEDIUM_PROVIDES_TARGET_AND_TRAJECTORY = Set.of(Socket.TARGET, Socket.TRAJECTORY);
    private static final Set<Socket> MEDIUM_PROVIDES_TARGET = Set.of(Socket.TARGET);
    private static final Set<Socket> MEDIUM_REQUIRES = Set.of(Socket.TRAJECTORY);

    private static final Set<Socket> EFFECT_PROVIDES = Set.of();
    private static final Set<Socket> EFFECT_REQUIRES = Set.of(Socket.TARGET);

    private static final Set<Socket> MODIFIER_PROVIDES = Set.of();
    private static final Set<Socket> MODIFIER_REQUIRES = Set.of();

    /** Common parameter definitions */
    private static final ParameterDef POTENCY = new ParameterDef("potency", "Potency", 1.0f, 5.0f, 1.0f, 0.5f);
    private static final ParameterDef DURATION = new ParameterDef("duration", "Duration", 1.0f, 10.0f, 1.0f, 0.3f);

    /** Initialization */
    public static void init() {
        // Root (special, non-selectable)
        register(new SpellComponentDefinition(
                ROOT_ID, "Root",
                SpellComponentType.MEDIUM, 0,
                SpellComponentDefinition.MEDIUM_CHILDREN,
                focalIcon("root"),
                ROOT_PROVIDES, ROOT_REQUIRES,
                List.of()));

        // Mediums (delivery methods)
        // Touch: short range raycast, provides target + trajectory (can chain)
        registerMedium("aversio", "Touch", 1, "touch",
                MEDIUM_PROVIDES_TARGET_AND_TRAJECTORY, MEDIUM_REQUIRES,
                List.of(new ParameterDef("range", "Range", 1.0f, 5.0f, 3.0f, 0.4f)));

        // Bolt: long range raycast, provides target only
        registerMedium("potentia", "Bolt", 2, "bolt",
                MEDIUM_PROVIDES_TARGET, MEDIUM_REQUIRES,
                List.of(new ParameterDef("range", "Range", 5.0f, 30.0f, 20.0f, 0.1f)));

        // Mine: block at trajectory, provides target
        registerMedium("vinculum", "Mine", 3, "mine",
                MEDIUM_PROVIDES_TARGET, MEDIUM_REQUIRES,
                List.of(new ParameterDef("range", "Range", 1.0f, 10.0f, 5.0f, 0.3f)));

        // Cloud: AoE at trajectory endpoint, provides target + trajectory
        registerMedium("alkimia", "Cloud", 3, "cloud",
                MEDIUM_PROVIDES_TARGET_AND_TRAJECTORY, MEDIUM_REQUIRES,
                List.of(
                        new ParameterDef("radius", "Radius", 1.0f, 8.0f, 3.0f, 0.5f),
                        new ParameterDef("range", "Range", 3.0f, 20.0f, 10.0f, 0.1f)));

        // Spellbat: homing projectile, provides target
        registerMedium("bestia", "Spellbat", 4, "spellbat",
                MEDIUM_PROVIDES_TARGET, MEDIUM_REQUIRES,
                List.of(new ParameterDef("range", "Range", 5.0f, 30.0f, 15.0f, 0.1f)));

        // Architect: block placement along trajectory, provides target + trajectory
        registerMedium("fabrico", "Architect", 2, "build",
                MEDIUM_PROVIDES_TARGET_AND_TRAJECTORY, MEDIUM_REQUIRES,
                List.of(new ParameterDef("range", "Range", 1.0f, 10.0f, 5.0f, 0.3f)));

        // Effects
        registerEffect("ignis", "Fire", 2, "fire", List.of(
                POTENCY, DURATION,
                new ParameterDef("burnDuration", "Burn Duration", 1.0f, 10.0f, 3.0f, 0.3f)));

        registerEffect("aer", "Air", 2, "air", List.of(
                POTENCY,
                new ParameterDef("knockback", "Knockback", 0.5f, 5.0f, 1.5f, 0.4f)));

        registerEffect("gelum", "Frost", 3, "frost", List.of(
                POTENCY, DURATION,
                new ParameterDef("slowAmplifier", "Slow Power", 0.0f, 4.0f, 1.0f, 0.6f)));

        registerEffect("terra", "Earth", 2, "earth", List.of(
                POTENCY, DURATION));

        registerEffect("vitium", "Flux", 1, "flux", List.of(
                POTENCY));

        registerEffect("mortuus", "Curse", 4, "curse", List.of(
                POTENCY, DURATION));

        registerEffect("victus", "Heal", 3, "heal", List.of(
                new ParameterDef("healAmount", "Heal Amount", 1.0f, 10.0f, 2.0f, 0.5f)));

        registerEffect("perditio", "Break", 2, "break", List.of(
                POTENCY));

        registerEffect("permutatio", "Exchange", 3, "exchange", List.of(
                POTENCY));

        registerEffect("alienis", "Rift", 4, "rift", List.of(
                POTENCY, DURATION));

        registerEffect("motus", "Motion", 2, "spray", List.of(
                POTENCY,
                new ParameterDef("force", "Force", 0.5f, 5.0f, 1.0f, 0.4f)));

        registerEffect("aqua", "Water", 2, "water", List.of(
                POTENCY, DURATION));

        // Modifiers
        registerModifier("power",    "Empower",   2, "potency");
        registerModifier("scatter",  "Split",     3, "scatter");
        registerModifier("chain",    "Chain",     3, "chain");
        registerModifier("stable",   "Stabilize", 1, "plan");
        registerModifier("delay",    "Delay",     1, "lingering");
        registerModifier("echo",     "Echo",      4, "burst");
        registerModifier("ricochet", "Ricochet",  2, "ricochet");
        registerModifier("sustain",  "Sustain",   3, "charge");
        registerModifier("entropy",  "Entropy",   2, "spread");

        Thaumaturge.LOGGER.info("Registered {} focal components", REGISTRY.size());
    }

    private static Identifier focalIcon(String iconName) {
        return Thaumaturge.identifier("textures/gui/focal_manipulator/" + iconName + ".png");
    }

    private static void registerMedium(String path, String name, int complexity, String iconName,
                                        Set<Socket> provides, Set<Socket> requires,
                                        List<ParameterDef> parameters) {
        register(new SpellComponentDefinition(
                AspectsLib.identifier(path), name,
                SpellComponentType.MEDIUM, complexity,
                SpellComponentDefinition.MEDIUM_CHILDREN,
                focalIcon(iconName),
                provides, requires,
                parameters));
    }

    private static void registerEffect(String path, String name, int complexity, String iconName,
                                        List<ParameterDef> parameters) {
        register(new SpellComponentDefinition(
                AspectsLib.identifier(path), name,
                SpellComponentType.EFFECT, complexity,
                SpellComponentDefinition.EFFECT_CHILDREN,
                focalIcon(iconName),
                EFFECT_PROVIDES, EFFECT_REQUIRES,
                parameters));
    }

    private static void registerModifier(String path, String name, int complexity, String iconName) {
        register(new SpellComponentDefinition(
                AspectsLib.identifier(path), name,
                SpellComponentType.MODIFIER, complexity,
                SpellComponentDefinition.NO_CHILDREN,
                focalIcon(iconName),
                MODIFIER_PROVIDES, MODIFIER_REQUIRES,
                List.of()));
    }

    /** Focus complexity limits per tier */
    public static int getComplexityLimit(String focusTier) {
        return switch (focusTier) {
            case "lesser"   -> 10;
            case "advanced" -> 25;
            case "greater"  -> 50;
            default         -> 10;
        };
    }
}
