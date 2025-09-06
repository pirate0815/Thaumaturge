package dev.overgrown.thaumaturge.item.apophenia.predicate;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.apophenia.ApopheniaItem;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.util.Identifier;

public class ApopheniaModelProvider {
    public static void register() {
        ModelPredicateProviderRegistry.register(
                new Identifier(Thaumaturge.MOD_ID, "open"),
                (stack, world, entity, seed) -> ApopheniaItem.isOpen(stack) ? 1.0F : 0.0F
        );
    }
}