package dev.overgrown.thaumaturge.client.tooltip;

import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.util.Identifier;
import java.util.Map;

public record AspectTooltipData(Map<Identifier, Integer> aspects) implements TooltipData {}
