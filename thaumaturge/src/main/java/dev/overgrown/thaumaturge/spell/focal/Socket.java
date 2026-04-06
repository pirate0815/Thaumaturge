package dev.overgrown.thaumaturge.spell.focal;

/**
 * Describes the data a spell component provides to or requires from its neighbors
 * in the focal manipulator's spell tree.
 *
 * <ul>
 *   <li>{@link #TARGET}: A reference to one or more entities/blocks the spell acts upon.</li>
 *   <li>{@link #TRAJECTORY}: A directional ray or path used by mediums to acquire targets.</li>
 * </ul>
 */
public enum Socket {
    TARGET,
    TRAJECTORY
}
