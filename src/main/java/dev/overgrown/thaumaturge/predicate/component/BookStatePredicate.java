package dev.overgrown.thaumaturge.predicate.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.overgrown.thaumaturge.component.BookStateComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.predicate.component.ComponentPredicate;

/**
	This component predicate check the {@link BookStateComponent} properties of an item stack
 */
public record BookStatePredicate(boolean open) implements ComponentPredicate {

	public static final Codec<BookStatePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.BOOL.fieldOf("open").forGetter(BookStatePredicate::open)
	).apply(instance, BookStatePredicate::new));

	@Override
	public boolean test(ComponentsAccess components) {

		BookStateComponent bookStateComponent = components.get(ModComponents.BOOK_STATE);

		if (bookStateComponent != null) {
			return bookStateComponent.open() == this.open();
		}

		else {
			return false;
		}

	}

}
