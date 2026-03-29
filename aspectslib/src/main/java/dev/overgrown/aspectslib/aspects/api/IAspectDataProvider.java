package dev.overgrown.aspectslib.aspects.api;


import dev.overgrown.aspectslib.aspects.data.AspectData;
import dev.overgrown.aspectslib.mixin.aspects.ItemStackMixin;

/**
 * Interface for objects that can provide aspect data.
 * Implemented via mixin on ItemStack.
 * <p>
 * Implemented by:
 * <li>{@link ItemStackMixin} (for ItemStack)</li>
 * <br>
 * Public API access via {@link AspectsAPI}
 * </p>
 */
public interface IAspectDataProvider {
    /**
     * Gets the aspect data for this provider
     * @return The aspect data, or AspectData.DEFAULT if none
     */
    AspectData aspectslib$getAspectData();

    /**
     * Sets the aspect data for this provider
     * @param data The aspect data to set, or null to clear
     */
    void aspectslib$setAspectData(AspectData data);
}