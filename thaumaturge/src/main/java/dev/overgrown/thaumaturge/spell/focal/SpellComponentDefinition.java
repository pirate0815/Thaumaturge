package dev.overgrown.thaumaturge.spell.focal;

import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Set;

/**
 * Static definition of a spell component that can be placed in the focal manipulator's spell tree.
 *
 * @param id              unique identifier (e.g., "aspectslib:ignis")
 * @param name            display name (e.g., "Fire")
 * @param type            MEDIUM, EFFECT, or MODIFIER
 * @param baseComplexity  base complexity cost before settings/repeat penalty
 * @param allowedChildren which component types may be placed as children of this node
 * @param icon            texture identifier for the 32x32 GUI icon
 * @param provides        sockets this component outputs (TARGET, TRAJECTORY)
 * @param requires        sockets this component needs from its parent
 * @param parameters      tunable parameters for this component (may be empty)
 */
public record SpellComponentDefinition(
        Identifier id,
        String name,
        SpellComponentType type,
        int baseComplexity,
        Set<SpellComponentType> allowedChildren,
        Identifier icon,
        Set<Socket> provides,
        Set<Socket> requires,
        List<ParameterDef> parameters
) {
    /** Mediums can hold effects, modifiers, and chained mediums. */
    public static final Set<SpellComponentType> MEDIUM_CHILDREN =
            Set.of(SpellComponentType.MEDIUM, SpellComponentType.EFFECT, SpellComponentType.MODIFIER);

    /** Effects can hold modifiers. */
    public static final Set<SpellComponentType> EFFECT_CHILDREN =
            Set.of(SpellComponentType.MODIFIER);

    /** Modifiers are leaf nodes. */
    public static final Set<SpellComponentType> NO_CHILDREN = Set.of();
}
