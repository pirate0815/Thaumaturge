package dev.overgrown.thaumaturge.block.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public interface AspectContainer {


    /** Aspects a container either has
     *  or would like to have present
     * @return set of aspects identifiers
     */
    @Unmodifiable Set<Identifier> getAspects();

    /** Queries the level of an aspect in a container
     * @param aspect the identifier of the aspect
     * @return the amount in it
     */
    int getAspectLevel(@NotNull Identifier aspect);


    /** Queries for an aspect what level the container desires
     * @param aspect the identifier of the aspect
     * @return returns the level or null no level is desired
     */
    @Nullable Integer getDesiredAspectLeve(@NotNull Identifier aspect);


    /** Queries the AspectContainer for general support of reducing aspects levels
     * @return boolean returns true if in principle aspect levels can be reduced
     */
    boolean canReduceAspectLevels();

    /** Queries how much the aspect level in container can be reduced
     * @param aspect the identifier of the aspect
     * @return amount that can be removed
     */
    int getReducibleAspectLevel(@NotNull Identifier aspect);

    /** Directly removes Aspect from a container,
     *  {@link #getReducibleAspectLevel} must be called first,
     *  to determine how much aspect can be removed.
     * @param aspect identifier of the aspect level that should be reduced
     * @param amount amount that is to be reduced in the container
     */
    void reduceAspectLevel(@NotNull Identifier aspect, int amount);

    /** Tries to increase the level of Aspect in a container, returns the increased amount
     * @param aspect identifier of the aspect to be increased
     * @param amount amount that is intended to be increased
     * @return the actual amount of aspect that was increased in the container
     */
    int increaseAspectLevel(@NotNull Identifier aspect, int amount);

}
