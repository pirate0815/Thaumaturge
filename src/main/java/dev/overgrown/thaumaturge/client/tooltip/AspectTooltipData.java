package dev.overgrown.thaumaturge.client.tooltip;

import dev.overgrown.thaumaturge.data.Aspect;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.registry.entry.RegistryEntry;

public record AspectTooltipData(Object2IntMap<RegistryEntry<Aspect>> aspects) implements TooltipData {}