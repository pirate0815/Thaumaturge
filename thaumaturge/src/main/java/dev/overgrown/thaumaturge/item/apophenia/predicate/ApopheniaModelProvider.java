package dev.overgrown.thaumaturge.item.apophenia.predicate;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.integration.modonomicon.ModonomiconIntegration;
import dev.overgrown.thaumaturge.item.apophenia.ApopheniaItem;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class ApopheniaModelProvider {
    public static void register() {
        ModelPredicateProviderRegistry.register(
                new Identifier(Thaumaturge.MOD_ID, "open"),
                (stack, world, entity, seed) -> {
                    // Use Modonomicon's open state if available, otherwise fall back to original behavior
                    if (ModonomiconIntegration.isModonomiconLoaded()) {
                        return ModonomiconIntegration.isBookOpen(stack) ? 1.0F : 0.0F;
                    } else {
                        return ApopheniaItem.isOpen(stack) ? 1.0F : 0.0F;
                    }
                }
        );

        // Also register Modonomicon's open state property for compatibility
        if (ModonomiconIntegration.isModonomiconLoaded()) {
            ModelPredicateProviderRegistry.register(
                    new Identifier("modonomicon", "open_state"),
                    (stack, world, entity, seed) -> {
                        return ModonomiconIntegration.isBookOpen(stack) ? 1.0F : 0.0F;
                    }
            );
        }
    }
}