package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.block.ModBlockEntities;
import dev.overgrown.thaumaturge.block.ModBlocks;
import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.data.AspectManager;
import dev.overgrown.thaumaturge.data.CustomItemTagManager;
import dev.overgrown.thaumaturge.data.ModRegistries;
import dev.overgrown.thaumaturge.item.ModItemGroups;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.networking.SyncAspectIdentifierPacket;
import dev.overgrown.thaumaturge.recipe.Recipe;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thaumaturge implements ModInitializer {
	public static final String MOD_ID = "thaumaturge";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModRegistries.register();
		ModItems.register();
		ModBlocks.register();
		ModItemGroups.register();
		ModComponents.register();
		registerRecipes();
		ModBlockEntities.register();

		PayloadTypeRegistry.playS2C().register(SyncAspectIdentifierPacket.ID, SyncAspectIdentifierPacket.PACKET_CODEC);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SyncAspectIdentifierPacket.ID.id(), (player, joined) -> SyncAspectIdentifierPacket.sendMap(player, AspectManager.NAME_TO_ID));

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("aspects"), AspectManager::new);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("item_aspects"), CustomItemTagManager::new);
	}

	private void registerRecipes() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			RegistryWrapper.WrapperLookup registries = server.getRegistryManager();
			RegistryWrapper.Impl<Aspect> aspectsRegistry = registries.getOrThrow(ModRegistries.ASPECTS);

			RegistryKey<Aspect> praecantatioKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("praecantatio"));
			RegistryKey<Aspect> vitreusKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("vitreus"));
			RegistryKey<Aspect> auramKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("auram"));
			RegistryKey<Aspect> aerKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("aer"));

			RegistryEntry.Reference<Aspect> praecantatio = aspectsRegistry.getOptional(praecantatioKey)
					.orElseThrow(() -> new IllegalStateException("Praecantatio aspect not found"));
			RegistryEntry.Reference<Aspect> vitreus = aspectsRegistry.getOptional(vitreusKey)
					.orElseThrow(() -> new IllegalStateException("Vitreus aspect not found"));
			RegistryEntry.Reference<Aspect> auram = aspectsRegistry.getOptional(auramKey)
					.orElseThrow(() -> new IllegalStateException("Auram aspect not found"));
			RegistryEntry.Reference<Aspect> aer = aspectsRegistry.getOptional(aerKey)
					.orElseThrow(() -> new IllegalStateException("Aer aspect not found"));

			new Recipe.Builder()
					.catalyst(ModItems.ORDO_VIS_CRYSTAL)
					.requiresFluid(VesselBlock.FluidType.WATER, 3)
					.requires(praecantatio, 10)
					.requires(vitreus, 20)
					.requires(auram, 5)
					.output(new ItemStack(ModItems.LESSER_FOCI))
					.register();

			new Recipe.Builder()
					.catalyst(Items.AMETHYST_SHARD)
					.requires(aer, 1)
					.output(new ItemStack(ModItems.AER_ASPECT_SHARD))
					.register();
		});
	}
}
