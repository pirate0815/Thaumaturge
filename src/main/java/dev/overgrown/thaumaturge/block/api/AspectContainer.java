package dev.overgrown.thaumaturge.block.api;

import java.util.Map;

public interface AspectContainer {

    Map<String, Integer> getAspects();

    /** Discovers how much of an aspect can be removed from a container
     * @param aspect the name of the aspect
     * @return amount that can be removed
     */
    int getRemovableAspectCount(String aspect);

    /** Directly removes Aspect from a container,
     *  {@link #getRemovableAspectCount} must be called first,
     *  to determine how much aspect can be removed.
     * @param aspect name of the aspect to be removed
     * @param amount amount that should be removed
     */
    void removeAspect(String aspect, int amount);

    /** Tries to add more Aspect to a container, return the increased amount
     * @param aspect name of the aspect to be added
     * @param amount amount that is intended to be added
     * @return the actual amount of aspect that was added to the container
     */
    int addAditionalAspect(String aspect, int amount);

}
