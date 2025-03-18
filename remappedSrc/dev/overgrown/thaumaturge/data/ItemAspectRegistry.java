package dev.overgrown.thaumaturge.data;

import dev.overgrown.thaumaturge.component.AspectComponent;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.stream.Stream;

/**
 * This class pairs AspectComponents to Identifiers, used in applying custom AspectComponents to items based on the items
 * Identifier.
 */
public class ItemAspectRegistry {

    /**
     * Internal storage for the mappings between Identifiers and AspectComponents
     */
    private static final HashMap<Identifier, AspectComponent> idToAspect = new HashMap<>();

    /**
     * Registers a new association between an `Identifier` and an `AspectComponent`.
     *
     * @param id     The `Identifier` of the item to which the aspect will be applied.
     * @param aspect The `AspectComponent` to associate with the item.
     * @return The registered `AspectComponent`.
     * @throws IllegalArgumentException If the `Identifier` is already registered.
     */
    public static AspectComponent register(Identifier id, AspectComponent aspect) {
        if(idToAspect.containsKey(id)) {
            //idToAspect.put(id, idToAspect.get(id).addAspect(aspect));
            return aspect;
        }
        idToAspect.put(id, aspect);
        return aspect;
    }

    /**
     * Updates an existing association between an `Identifier` and an `AspectComponent`. If the `Identifier` is already
     * registered, the old `AspectComponent` is removed and replaced with the new one.
     *
     * @param id     The `Identifier` of the item to update.
     * @param aspect The new `AspectComponent` to associate with the item.
     */
    protected static void update(Identifier id, AspectComponent aspect) {
        if(idToAspect.containsKey(id)) {
            AspectComponent old = idToAspect.get(id);
            idToAspect.remove(id);
        }
        register(id, aspect);
    }

    /**
     * Removes the association for the specified `Identifier`.
     *
     * @param id The `Identifier` of the item to remove from the registry.
     */
    protected static void remove(Identifier id) {
        idToAspect.remove(id);
    }

    /**
     * Returns the number of registered item-aspect associations.
     *
     * @return The number of entries in the registry.
     */
    public static int size() {
        return idToAspect.size();
    }

    /**
     * Provides a stream of all registered `Identifier` objects.
     *
     * @return A stream of `Identifier` objects.
     */
    public static Stream<Identifier> identifiers() {
        return idToAspect.keySet().stream();
    }

    /**
     * Provides a set of all registered entries (key-value pairs) in the registry.
     *
     * @return A set of `Map.Entry` objects representing the registered associations.
     */
    public static Set<Map.Entry<Identifier, AspectComponent>> entries() {
        return idToAspect.entrySet();
    }

    /**
     * Provides a list of all registered `AspectComponent` objects.
     *
     * @return A list of `AspectComponent` objects.
     */
    public static List<AspectComponent> values() {
        return idToAspect.values().stream().toList();
    }

    /**
     * Retrieves the `AspectComponent` associated with the specified `Identifier`.
     *
     * @param id The `Identifier` of the item to look up.
     * @return The associated `AspectComponent`.
     * @throws IllegalArgumentException If the `Identifier` is not registered.
     */

    public static AspectComponent get(Identifier id) {
        if(!idToAspect.containsKey(id)) {
            throw new IllegalArgumentException("Could not get aspect data from id '" + id.toString() + "', as it was not registered!");
        }

        return idToAspect.get(id);
    }

    /**
     * Retrieves the `Identifier` associated with the specified `AspectComponent`.
     *
     * @param aspect The `AspectComponent` to look up.
     * @return The associated `Identifier`.
     * @throws IllegalArgumentException If the `AspectComponent` is not registered.
     */
    public static Identifier getId(AspectComponent aspect) {
        if(!idToAspect.containsValue(aspect)) {

        }
        Optional<Map.Entry<Identifier, AspectComponent>> entryOptional = idToAspect.entrySet().stream().filter((aspectEntry) -> Objects.equals(aspectEntry.getValue(), aspect)).findFirst();
        if(entryOptional.isPresent()) {
            return entryOptional.get().getKey();
        } else {
            throw new IllegalArgumentException("Could not get identifier from essence '" + aspect.toString() + "', as it was not registered!");
        }
    }

    /**
     * Checks if the registry contains an association for the specified `Identifier`.
     *
     * @param id The `Identifier` to check.
     * @return `true` if the `Identifier` is registered, otherwise `false`.
     */
    public static boolean contains(Identifier id) {
        return idToAspect.containsKey(id);
    }

    /**
     * Clears all registered associations from the registry.
     */
    public static void clear() {
        idToAspect.clear();
    }

    /**
     * Resets the registry by clearing all registered associations. This is an alias for {@link #clear()}.
     */
    public static void reset() {
        clear();
    }
}
