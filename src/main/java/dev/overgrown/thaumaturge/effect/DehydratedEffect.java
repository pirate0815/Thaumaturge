package dev.overgrown.thaumaturge.effect;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.util.Identifier;

import java.util.UUID;

public class DehydratedEffect extends StatusEffect {
    private static final UUID DEHYDRATED_UUID = UUID.fromString("1eabc8e8-0a3d-4b8d-9f2a-3e8e9f7b3a7d");
    private static final String MODIFIER_ID = "thaumaturge:dehydrated_health_penalty";

    public DehydratedEffect() {
        super(StatusEffectCategory.HARMFUL, 0xFFA500);
        // Add a static attribute modifier (adjust value as needed)
        this.addAttributeModifier(
                EntityAttributes.MAX_HEALTH,
                Identifier.of(MODIFIER_ID),
                -0.05, // Static reduction of 5% of base health
                EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );
    }
}