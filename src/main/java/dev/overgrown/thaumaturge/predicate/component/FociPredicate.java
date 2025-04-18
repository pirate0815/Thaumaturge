/**
 * FociPredicate.java
 * <p>
 * This class implements a component predicate that checks if a gauntlet has foci installed.
 * This predicate can be used in data packs to conditionally execute logic based on
 * whether a gauntlet has any foci installed.
 *
 * @see dev.overgrown.thaumaturge.predicate.component.ModComponentPredicateTypes#FOCI_STATE
 * @see dev.overgrown.thaumaturge.component.GauntletComponent
 */
package dev.overgrown.thaumaturge.predicate.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.predicate.component.ComponentPredicate;

public record FociPredicate(boolean hasFoci) implements ComponentPredicate {
    /**
     * Codec for serializing/deserializing this predicate
     * Expects a boolean field "has_foci" that determines the expected state
     */
    public static final Codec<FociPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("has_foci").forGetter(FociPredicate::hasFoci)
    ).apply(instance, FociPredicate::new));

    /**
     * Tests if a component meets this predicate's condition
     *
     * @param components The component access to test
     * @return true if the gauntlet component matches the expected state (has or doesn't have foci)
     */
    @Override
    public boolean test(ComponentsAccess components) {
        // Get the gauntlet component
        GauntletComponent component = components.get(ModComponents.GAUNTLET_STATE);

        // Check if component exists and whether it has foci matches our expectation
        return component != null && (component.fociCount() > 0) == this.hasFoci();
    }
}