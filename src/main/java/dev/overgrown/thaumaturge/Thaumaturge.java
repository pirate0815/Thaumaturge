package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.registry.ModItems;
import dev.overgrown.thaumaturge.spell.impl.ignis.IgnisEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ScatterModifierEffect;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.utils.SpellHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thaumaturge implements ModInitializer {
    public static final String MOD_ID = "thaumaturge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier identifier(String path) {
        return new Identifier(Thaumaturge.MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Register all items
        ModItems.initialize();

        // Register spell components
        registerAspectEffects();
        registerModifierEffects();

        // Register packet handler
        ServerPlayNetworking.registerGlobalReceiver(SpellCastPacket.ID, (server, player, handler, buf, responseSender) -> {
            SpellCastPacket packet = new SpellCastPacket(buf);
            server.execute(() -> SpellHandler.castSpell(player, packet.getHand(), packet.getSpellKey()));
        });

        LOGGER.info("Thaumaturge initialized!");
    }

    private void registerAspectEffects() {
        AspectRegistry.register(AspectsLib.identifier("ignis"), new IgnisEffect());
    }

    private void registerModifierEffects() {
        ModifierRegistry.register(identifier("power"), new PowerModifierEffect());
        ModifierRegistry.register(identifier("scatter"), new ScatterModifierEffect());
        ModifierRegistry.register(identifier("stable"), context -> {}); // Stable does nothing
    }
}