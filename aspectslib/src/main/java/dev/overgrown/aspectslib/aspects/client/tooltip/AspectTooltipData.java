package dev.overgrown.aspectslib.aspects.client.tooltip;

import dev.overgrown.aspectslib.aspects.data.AspectData;
import net.minecraft.client.item.TooltipData;

/**
 * Holds aspect data for tooltip rendering.
 * <p>
 * Passed to {@link AspectTooltipComponent} for rendering.
 * </p>
 */
public record AspectTooltipData(AspectData aspectData) implements TooltipData {
}