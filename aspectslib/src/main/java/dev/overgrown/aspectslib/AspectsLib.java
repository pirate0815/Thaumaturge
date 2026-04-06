package dev.overgrown.aspectslib;

import com.google.gson.Gson;
import dev.overgrown.aspectslib.aether.AetherEvents;
import dev.overgrown.aspectslib.aether.AetherManager;
import dev.overgrown.aspectslib.aspects.data.AspectManager;
import dev.overgrown.aspectslib.aspects.data.UniversalAspectManager;
import dev.overgrown.aspectslib.aspects.networking.SyncAspectIdentifierPacket;
import dev.overgrown.aspectslib.aspects.recipe.RecipeAspectManager;
import dev.overgrown.aspectslib.command.AspectDebugCommand;
import dev.overgrown.aspectslib.command.RecipeAspectCommand;
import dev.overgrown.aspectslib.command.TagDumpCommand;
import dev.overgrown.aspectslib.registry.ModEntities;
import dev.overgrown.aspectslib.registry.ModItems;
import dev.overgrown.aspectslib.resonance.ResonanceManager;
import dev.overgrown.aspectslib.spell.SpellRegistry;
import dev.overgrown.aspectslib.spell.aether.PersonalAetherManager;
import dev.overgrown.aspectslib.spell.example.ExampleIgnisSpell;
import dev.overgrown.aspectslib.spell.modifier.ModifierRegistry;
import dev.overgrown.aspectslib.spell.networking.SpellNetworking;
import dev.overgrown.aspectslib.spell.resonance.VolatileResonanceRegistry;
import dev.overgrown.aspectslib.spell.unraveling.UnravelingTracker;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AspectsLib implements ModInitializer {

	public static final String MOD_ID = "aspectslib";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final Gson GSON = new Gson();

	/** Helper for creating namespaced identifiers. */
	public static Identifier identifier(String path) {
		return new Identifier(MOD_ID, path);
	}

	@Override
	public void onInitialize() {

		// Items and entities
		ModItems.initialize();
		ModEntities.register();

		// Spell infrastructure (Modifier registry must come before SpellNetworking and any spell registration)
		ModifierRegistry.init();

		// Force static initialization of VolatileResonanceRegistry so the six
		// canonical pairs are registered before any spell validation runs.
		// A no-arg method reference that doesn't discard a result achieves this
		// without a "result ignored" warning.
		int _vpCount = VolatileResonanceRegistry.all().size(); // triggers <clinit>

		// Personal Aether pool regeneration ticks
		PersonalAetherManager.initialize();

		// Unraveling stress tracker ticks + NBT persistence via mixin
		UnravelingTracker.initialize();

		// Network channels (consuming mods register their own receivers)
		SpellNetworking.init();

		// Example spell registration (will remove eventually)
		// Shows consuming mods how to register a spell correctly.
		SpellRegistry.register(new ExampleIgnisSpell());

		// Commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			AspectDebugCommand.register(dispatcher, registryAccess);
			RecipeAspectCommand.register(dispatcher, registryAccess);
			TagDumpCommand.register(dispatcher);
		});

		// Data sync to joining players
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> {
			try {
				SyncAspectIdentifierPacket.sendAllData(player);
				LOGGER.debug("Sent aspect data to player: {}", player.getName().getString());
			} catch (Exception e) {
				LOGGER.error("Failed to send aspect data to player {}: {}",
						player.getName().getString(), e.getMessage());
			}
		});

		// Data loaders (resource reload listeners)
		// Aspect definitions must load first; everything else depends on them.
		ResourceManagerHelper server = ResourceManagerHelper.get(ResourceType.SERVER_DATA);

		server.registerReloadListener(new AspectManager());
		server.registerReloadListener(new UniversalAspectManager());
		server.registerReloadListener(new ResonanceManager());
		server.registerReloadListener(new AetherManager());
		server.registerReloadListener(new RecipeAspectManager());

		// Subsystem initialization
		AetherEvents.initialize();
		RecipeAspectManager.initialize();

		LOGGER.info("AspectsLib initialized — spell system active.");
	}
}