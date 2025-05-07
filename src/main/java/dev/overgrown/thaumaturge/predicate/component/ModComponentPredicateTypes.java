package dev.overgrown.thaumaturge.predicate.component;

import com.mojang.serialization.Codec;
import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.predicate.component.ComponentPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModComponentPredicateTypes {
	/**
	 * Registers all component predicate types defined in this class
	 */
	public static void register() {
		// Currently empty as registration happens through static initialization
	}

	/**
	 * Predicate for checking foci state in gauntlets
	 * Can be used in data packs to conditionally execute logic based on whether
	 * foci are installed in a gauntlet
	 */
	public static final ComponentPredicate.Type<FociPredicate> FOCI_STATE = register("foci_state", FociPredicate.CODEC);

	public static final ComponentPredicate.Type<BookStatePredicate> BOOK_STATE = register("book_state", BookStatePredicate.CODEC);

	/**
	 * Helper method to register a component predicate type
	 *
	 * @param path The path part of the identifier for this predicate
	 * @param codec The codec used for serialization/deserialization
	 * @return The registered component predicate type
	 */
	private static <T extends ComponentPredicate> ComponentPredicate.Type<T> register(String path, Codec<T> codec) {
		return Registry.register(Registries.DATA_COMPONENT_PREDICATE_TYPE, Thaumaturge.identifier(path), new ComponentPredicate.Type<>(codec));
	}

}
