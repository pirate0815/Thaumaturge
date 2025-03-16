package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.block.ModBlocks;
import dev.overgrown.thaumaturge.block.vessel.VesselRecipe;
import dev.overgrown.thaumaturge.block.vessel.VesselRecipeSerializer;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.data.AspectManager;
import dev.overgrown.thaumaturge.data.CustomItemTagManager;
import dev.overgrown.thaumaturge.data.ModRegistries;
import dev.overgrown.thaumaturge.item.ModItemGroups;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.networking.SyncAspectIdentifierPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.overgrown.thaumaturge.block.vessel.VesselBlock.VesselBlockEntity;

public class Thaumaturge implements ModInitializer {
	public static final String MOD_ID = "thaumaturge";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}

	// Updated RecipeType registration
	public static final RecipeType<VesselRecipe> VESSEL_RECIPE_TYPE = Registry.register(
			Registries.RECIPE_TYPE,
			identifier("vessel"),
			new RecipeType<VesselRecipe>() {
				@Override
				public String toString() {
					return identifier("vessel").toString();
				}
			}
	);

	// Updated RecipeSerializer registration
	public static final RecipeSerializer<VesselRecipe> VESSEL_RECIPE_SERIALIZER = Registry.register(
			Registries.RECIPE_SERIALIZER,
			identifier("vessel_recipe"),
			VesselRecipeSerializer.INSTANCE
	);

	public static final BlockEntityType<VesselBlockEntity> VESSEL_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			identifier("vessel"),
			FabricBlockEntityTypeBuilder.create(VesselBlockEntity::new, ModBlocks.VESSEL).build()
	);

	@Override
	public void onInitialize() {
		ModRegistries.register();
		ModItems.register();
		ModBlocks.register();
		ModItemGroups.register();
		ModComponents.register();

		PayloadTypeRegistry.playS2C().register(SyncAspectIdentifierPacket.ID, SyncAspectIdentifierPacket.PACKET_CODEC);

		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SyncAspectIdentifierPacket.ID.id(), (player, joined) -> {
			SyncAspectIdentifierPacket.sendMap(player, AspectManager.NAME_TO_ID);
		});

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("aspects"), AspectManager::new);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("item_aspects"), CustomItemTagManager::new);
	}
}
