package dev.overgrown.thaumaturge.mixin.client;

import dev.overgrown.thaumaturge.component.AspectComponent;
import dev.overgrown.thaumaturge.client.tooltip.AspectTooltipData;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.compat.modmenu.config.AspectConfig;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.data.ModRegistries;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.component.FociComponent;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.RegistryWrapper;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.Optional;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public abstract class ItemStackClientMixin {

    @Inject(method = "getTooltipData", at = @At("HEAD"), cancellable = true)
    private void addAspectTooltipData(CallbackInfoReturnable<Optional<TooltipData>> cir) {
        ItemStack stack = (ItemStack) (Object) this;

        // Check for AspectComponent first
        AspectComponent component = stack.getOrDefault(ModComponents.ASPECT, AspectComponent.DEFAULT);

        // If no aspects, check for FociComponent
        if (component.isEmpty()) {
            FociComponent fociComp = stack.get(ModComponents.FOCI_COMPONENT);
            if (fociComp != null && fociComp.aspectId() != null) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client.world != null) {
                    RegistryWrapper.Impl<Aspect> aspectRegistry = client.world.getRegistryManager().getOrThrow(ModRegistries.ASPECTS);
                    RegistryKey<Aspect> aspectKey = RegistryKey.of(ModRegistries.ASPECTS, fociComp.aspectId());
                    RegistryEntry.Reference<Aspect> aspectEntry = aspectRegistry.getOptional(aspectKey).orElse(null);

                    if (aspectEntry != null) {
                        Object2IntOpenHashMap<RegistryEntry<Aspect>> aspectsMap = new Object2IntOpenHashMap<>();
                        aspectsMap.put(aspectEntry, 1); // Assuming each Foci has 1 aspect
                        component = new AspectComponent(aspectsMap);
                    }
                }
            }
        }

        if (component.isEmpty()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }

        PlayerEntity player = client.player;

        boolean hasAspectLens = thaumaturge$hasAspectLens(player);
        boolean hasHeadGear = thaumaturge$hasAethericGogglesOrMonocle(player);

        if (hasAspectLens || hasHeadGear) {
            cir.setReturnValue(Optional.of(new AspectTooltipData(component.getMap())));
        }

        boolean shouldShow = AspectConfig.ALWAYS_SHOW_ASPECTS || hasAspectLens || hasHeadGear;

        if (shouldShow) {
            cir.setReturnValue(Optional.of(new AspectTooltipData(component.getMap())));
        }
    }

    @Unique
    private boolean thaumaturge$hasAspectLens(PlayerEntity player) {
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack item = player.getInventory().getStack(i);
            if (item.getItem() == ModItems.ASPECT_LENS) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean thaumaturge$hasAethericGogglesOrMonocle(PlayerEntity player) {
        ItemStack headStack = player.getEquippedStack(EquipmentSlot.HEAD);
        return headStack.getItem() == ModItems.AETHERIC_GOGGLES;
    }
}
