package dev.overgrown.thaumaturge.predicate.component;

import com.mojang.serialization.Codec;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModComponentPredicateTypes {
	public static void register() {

	}

	public static final ComponentPredicate.Type<FociPredicate> FOCI_STATE = register("foci_state", FociPredicate.CODEC);

	public static final ComponentPredicate.Type<BookStatePredicate> BOOK_STATE = register("book_state", BookStatePredicate.CODEC);

	private static <T extends ComponentPredicate> ComponentPredicate.Type<T> register(String path, Codec<T> codec) {
		return Registry.register(Registries.DATA_COMPONENT_PREDICATE_TYPE, Thaumaturge.identifier(path), new ComponentPredicate.Type<>(codec));
	}

}
