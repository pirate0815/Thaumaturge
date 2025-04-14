package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.component.AspectComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.data.ItemAspectRegistry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Item.class)
public abstract class ItemMixin {

    /**
     * This declaration shadows (references a method/field from what you are mixing into) the
     * registryEntry field.
     */
    @Shadow @Final private RegistryEntry.Reference<Item> registryEntry;

    /**
     * This method will add the AspectComponent to the Item from the ItemAspectRegistry.
     */
    @Inject(method = "getComponents", at = @At("RETURN"), cancellable = true)
    private void getWithAspects(CallbackInfoReturnable<ComponentMap> cir) {
        if(cir.getReturnValue().contains(ModComponents.ASPECT)) return;

        ComponentMap.Builder builder = ComponentMap.builder();
        builder.addAll(cir.getReturnValue());

        AspectComponent aspectComponent = new AspectComponent(new Object2IntOpenHashMap<>());
        // TODO: Find a better implementation than iterating over the registry. this can be resource intensive at present.
        for (Map.Entry<Identifier, AspectComponent> entry : ItemAspectRegistry.entries()) {
            Identifier id = entry.getKey();
            AspectComponent itemAspectComponent = entry.getValue();


            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
            TagKey<Item> tagKey = TagKey.of(RegistryKeys.ITEM, id);

            if(this.registryEntry.matchesKey(itemKey)) {
                aspectComponent = aspectComponent.addAspect(itemAspectComponent);
            }
            if(this.registryEntry.isIn(tagKey)) {
                aspectComponent = aspectComponent.addAspect(itemAspectComponent);
            }

        }

        builder.add(ModComponents.ASPECT, aspectComponent);

        cir.setReturnValue(builder.build());
    }
}
