package dev.overgrown.thaumaturge;

import dev.overgrown.thaumaturge.block.ModBlockEntities;
import dev.overgrown.thaumaturge.block.ModBlocks;
import dev.overgrown.thaumaturge.block.vessel.VesselBlock;
import dev.overgrown.thaumaturge.component.FociComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.data.Aspect;
import dev.overgrown.thaumaturge.data.AspectManager;
import dev.overgrown.thaumaturge.data.CustomItemTagManager;
import dev.overgrown.thaumaturge.data.ModRegistries;
import dev.overgrown.thaumaturge.event.ModEvents;
import dev.overgrown.thaumaturge.item.ModItemGroups;
import dev.overgrown.thaumaturge.item.ModItems;
import dev.overgrown.thaumaturge.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.networking.SyncAspectIdentifierPacket;
import dev.overgrown.thaumaturge.predicate.component.ModComponentPredicateTypes;
import dev.overgrown.thaumaturge.block.vessel.recipe.Recipe;
import dev.overgrown.thaumaturge.spell.SpellHandler;
import dev.overgrown.thaumaturge.spell.impl.aer.AerEffect;
import dev.overgrown.thaumaturge.spell.impl.alienis.AlienisEffect;
import dev.overgrown.thaumaturge.spell.impl.alkimia.AlkimiaEffect;
import dev.overgrown.thaumaturge.spell.impl.gelum.GelumEffect;
import dev.overgrown.thaumaturge.spell.impl.herba.HerbaEffect;
import dev.overgrown.thaumaturge.spell.impl.ignis.IgnisEffect;
import dev.overgrown.thaumaturge.spell.impl.metallum.MetallumEffect;
import dev.overgrown.thaumaturge.spell.impl.motus.MotusEffect;
import dev.overgrown.thaumaturge.spell.impl.perditio.PerditioEffect;
import dev.overgrown.thaumaturge.spell.impl.permutatio.PermutatioEffect;
import dev.overgrown.thaumaturge.spell.impl.potentia.PotentiaEffect;
import dev.overgrown.thaumaturge.spell.impl.vacuos.VacuosEffect;
import dev.overgrown.thaumaturge.spell.impl.victus.VictusEffect;
import dev.overgrown.thaumaturge.spell.impl.vinculum.VinculumEffect;
import dev.overgrown.thaumaturge.spell.impl.vitium.VitiumEffect;
import dev.overgrown.thaumaturge.spell.impl.volatus.VolatusEffect;
import dev.overgrown.thaumaturge.spell.impl.volatus.effect.VolatusFlightEffect;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifier;
import dev.overgrown.thaumaturge.spell.modifier.ScatterModifier;
import dev.overgrown.thaumaturge.spell.modifier.SimpleModifier;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.pattern.ModifierRegistry;
import dev.overgrown.thaumaturge.utils.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thaumaturge implements ModInitializer {
	public static final String MOD_ID = "thaumaturge";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier identifier(String path) {
		return Identifier.of(MOD_ID, path);
	}

	public static final RegistryEntry<StatusEffect> VOLATUS_FLIGHT_EFFECT =
			Registry.registerReference(Registries.STATUS_EFFECT, identifier("volatus_flight"), new VolatusFlightEffect());

	@Override
	public void onInitialize() {
		// Register all mod components, items, blocks, etc.
		ModRegistries.register();
		ModItems.register();
		ModBlocks.register();
		ModItemGroups.register();
		ModComponents.register();
		ModEvents.register();
		ModSounds.initialize();
		ModComponentPredicateTypes.register();
		registerRecipes();
		ModBlockEntities.register();

		registerAspectEffects();
		registerModifierEffects();

		// Register networking packets
		// For syncing aspect identifiers from server to client
		PayloadTypeRegistry.playS2C().register(SyncAspectIdentifierPacket.ID, SyncAspectIdentifierPacket.PACKET_CODEC);
		ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(SyncAspectIdentifierPacket.ID.id(), (player, joined) -> SyncAspectIdentifierPacket.sendMap(player, AspectManager.NAME_TO_ID));

		// Register resource reload listeners for loading aspect data from datapacks
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("aspects"), AspectManager::new);
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Thaumaturge.identifier("item_aspects"), CustomItemTagManager::new);

		// Register spell cast packet handling
		PayloadTypeRegistry.playC2S().register(SpellCastPacket.ID, SpellCastPacket.PACKET_CODEC);
		PayloadTypeRegistry.playS2C().register(SpellCastPacket.ID, SpellCastPacket.PACKET_CODEC);

		ServerPlayNetworking.registerGlobalReceiver(SpellCastPacket.ID, (packet, context) -> handleSpellCast(packet, context.player()));
	}

	private void registerAspectEffects() {
		AspectRegistry.register(Thaumaturge.identifier("ignis"), new IgnisEffect());
		AspectRegistry.register(Thaumaturge.identifier("potentia"), new PotentiaEffect());
		AspectRegistry.register(Thaumaturge.identifier("alienis"), new AlienisEffect());
		AspectRegistry.register(Thaumaturge.identifier("motus"), new MotusEffect());
		AspectRegistry.register(Thaumaturge.identifier("permutatio"), new PermutatioEffect());
		AspectRegistry.register(Thaumaturge.identifier("aer"), new AerEffect());
		AspectRegistry.register(Thaumaturge.identifier("alkimia"), new AlkimiaEffect());
		AspectRegistry.register(Thaumaturge.identifier("vitium"), new VitiumEffect());
		AspectRegistry.register(Thaumaturge.identifier("gelum"), new GelumEffect());
		AspectRegistry.register(Thaumaturge.identifier("victus"), new VictusEffect());
		AspectRegistry.register(Thaumaturge.identifier("vinculum"), new VinculumEffect());
		AspectRegistry.register(Thaumaturge.identifier("perditio"), new PerditioEffect());
		AspectRegistry.register(Thaumaturge.identifier("metallum"), new MetallumEffect());
		AspectRegistry.register(Thaumaturge.identifier("vacuos"), new VacuosEffect());
		AspectRegistry.register(Thaumaturge.identifier("herba"), new HerbaEffect());
		AspectRegistry.register(Thaumaturge.identifier("volatus"), new VolatusEffect());
		// ... other aspects
	}

	private void registerModifierEffects() {
		ModifierRegistry.register(Thaumaturge.identifier("scatter_resonance_modifier"), new ScatterModifier());
		ModifierRegistry.register(Thaumaturge.identifier("power_resonance_modifier"), new PowerModifier());
		ModifierRegistry.register(Thaumaturge.identifier("simple_resonance_modifier"), new SimpleModifier());
		// ... other modifiers
	}

	private void handleSpellCast(SpellCastPacket packet, ServerPlayerEntity player) {
		SpellHandler.tryCastSpell(player, packet.tier());
	}

	private void registerRecipes() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			RegistryWrapper.WrapperLookup registries = server.getRegistryManager();
			RegistryWrapper.Impl<Aspect> aspectsRegistry = registries.getOrThrow(ModRegistries.ASPECTS);

			for (RegistryEntry.Reference<Aspect> aspectEntry : aspectsRegistry.streamEntries().toList()) {
				Identifier aspectId = aspectEntry.registryKey().getValue();
				Item shardItem = Registries.ITEM.get(Thaumaturge.identifier(aspectId.getPath() + "_aspect_shard"));

				if (shardItem == Items.AIR) {
					Thaumaturge.LOGGER.warn("Missing aspect shard for {}", aspectId);
					continue;
				}

				// Register Foci recipes
				registerFociRecipe(ModItems.LESSER_FOCI, aspectEntry, aspectId);
				registerFociRecipe(ModItems.ADVANCED_FOCI, aspectEntry, aspectId);
				registerFociRecipe(ModItems.GREATER_FOCI, aspectEntry, aspectId);
			}

			RegistryKey<Aspect> aerKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("aer"));
			RegistryEntry.Reference<Aspect> aerEntry = aspectsRegistry.getOrThrow(aerKey);

			RegistryKey<Aspect> praecantatioKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("praecantatio"));
			RegistryEntry.Reference<Aspect> praecantatio = aspectsRegistry.getOrThrow(praecantatioKey);

			RegistryKey<Aspect> vitreusKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("vitreus"));
			RegistryEntry.Reference<Aspect> vitreusEntry = aspectsRegistry.getOrThrow(vitreusKey);

			RegistryKey<Aspect> auramKey = RegistryKey.of(ModRegistries.ASPECTS, Thaumaturge.identifier("auram"));
			RegistryEntry.Reference<Aspect> auramEntry = aspectsRegistry.getOrThrow(auramKey);

			new Recipe.Builder()
					.catalyst(ModItems.ORDO_ASPECT_SHARD)
					.requiresFluid(VesselBlock.FluidType.WATER, 3)
					.requires(praecantatio, 10)
					.requires(vitreusEntry, 20)
					.requires(auramEntry, 5)
					.output(new ItemStack(ModItems.LESSER_FOCI))
					.register();
		});
	}

	private void registerFociRecipe(Item fociItem, RegistryEntry.Reference<Aspect> aspectEntry, Identifier aspectId) {
		new Recipe.Builder()
				.catalyst(fociItem)
				.requires(aspectEntry, 1)
				.output(createFociStack(fociItem, aspectId))
				.register();
	}

	private ItemStack createFociStack(Item fociItem, Identifier aspectId) {
		ItemStack stack = new ItemStack(fociItem);
		stack.set(ModComponents.FOCI_COMPONENT, new FociComponent(aspectId, Thaumaturge.identifier("simple_resonance_modifier")));
		return stack;
	}
}
