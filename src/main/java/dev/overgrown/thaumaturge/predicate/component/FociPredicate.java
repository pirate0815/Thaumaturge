package dev.overgrown.thaumaturge.predicate.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.predicate.component.ComponentPredicate;

public record FociPredicate(boolean hasFoci) implements ComponentPredicate {

    public static final Codec<FociPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("has_foci").forGetter(FociPredicate::hasFoci)
    ).apply(instance, FociPredicate::new));

    @Override
    public boolean test(ComponentsAccess components) {
        GauntletComponent component = components.get(ModComponents.GAUNTLET_STATE);
        return component != null && component.hasFoci() == this.hasFoci();
    }
}