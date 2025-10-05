package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.recipe.VesselReactionRecipe;
import dev.overgrown.thaumaturge.recipe.VesselRecipe;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import dev.overgrown.thaumaturge.registry.ModItems;
import dev.overgrown.thaumaturge.registry.ModSounds;
import dev.overgrown.thaumaturge.spell.impl.aer.AerEffect;
import dev.overgrown.thaumaturge.spell.impl.ignis.IgnisEffect;
import dev.overgrown.thaumaturge.spell.impl.motus.MotusEffect;
import dev.overgrown.thaumaturge.spell.impl.perditio.PerditioEffect;
import dev.overgrown.thaumaturge.spell.impl.potentia.PotentiaEffect;
import dev.overgrown.thaumaturge.spell.impl.victus.VictusEffect;
import dev.overgrown.thaumaturge.spell.impl.vitium.VitiumEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ScatterModifierEffect;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.utils.SpellCooldownManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thaumaturge implements ModInitializer {
    public static final String MOD_ID = "thaumaturge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Items
        ModItems.initialize();

        // Blocks
        ModBlocks.initialize();

        // Sounds
        ModSounds.initialize();

        // Spell components
        registerAspectEffects();
        registerModifierEffects();

        // Networking
        SpellCastPacket.registerServer();

        // Register server tick event for cooldowns
        ServerTickEvents.END_SERVER_TICK.register(server -> SpellCooldownManager.tick());

        // Register player disconnect event to clean up cooldowns
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> SpellCooldownManager.removePlayer(handler.player.getUuid()));

        // Register recipe type and serializer
        Registry.register(Registries.RECIPE_SERIALIZER, identifier("vessel"), VesselRecipe.Serializer.INSTANCE);
        Registry.register(Registries.RECIPE_TYPE, identifier("vessel"), VesselRecipe.Type.INSTANCE);
        Registry.register(Registries.RECIPE_SERIALIZER, identifier("vessel_reaction"), VesselReactionRecipe.Serializer.INSTANCE);
        Registry.register(Registries.RECIPE_TYPE, identifier("vessel_reaction"), VesselReactionRecipe.Type.INSTANCE);

        LOGGER.info("Thaumaturge initialized!");
    }

    private void registerAspectEffects() {
        AspectRegistry.register(AspectsLib.identifier("aer"), new AerEffect());
        AspectRegistry.register(AspectsLib.identifier("ignis"), new IgnisEffect());
        AspectRegistry.register(AspectsLib.identifier("motus"), new MotusEffect());
        AspectRegistry.register(AspectsLib.identifier("perditio"), new PerditioEffect());
        AspectRegistry.register(AspectsLib.identifier("potentia"), new PotentiaEffect());
        AspectRegistry.register(AspectsLib.identifier("victus"), new VictusEffect());
        AspectRegistry.register(AspectsLib.identifier("vitium"), new VitiumEffect());
    }

    private void registerModifierEffects() {
        ModifierRegistry.register(identifier("power"), new PowerModifierEffect());
        ModifierRegistry.register(identifier("scatter"), new ScatterModifierEffect());
    }
}