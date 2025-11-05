package dev.overgrown.thaumaturge.block.api;

import net.minecraft.util.Identifier;

import java.util.Set;

public interface AspectContainer {


    /** Aspects a container either has
     *  or would like to have present
     * @return set of aspects identifiers
     */
    Set<Identifier> getAspects();

    /** Queries the level of an aspect in a container
     * @param aspect the identifier of the aspect
     * @return the amount in it
     */
    int getAspectLevel(Identifier aspect);

    /** Queries how much the aspect level in container can be reduced
     * @param aspect the identifier of the aspect
     * @return amount that can be removed
     */
    int getReducibleAspectLevel(Identifier aspect);

    /** Directly removes Aspect from a container,
     *  {@link #getReducibleAspectLevel} must be called first,
     *  to determine how much aspect can be removed.
     * @param aspect identifier of the aspect level that should be reduced
     * @param amount amount that is to be reduced in the container
     */
    void reduceAspectLevel(Identifier aspect, int amount);

    /** Tries to increase the level of Aspect in a container, returns the increased amount
     * @param aspect identifier of the aspect to be increased
     * @param amount amount that is intended to be increased
     * @return the actual amount of aspect that was increased in the container
     */
    int increaseAspectLevel(Identifier aspect, int amount);

}
