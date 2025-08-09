package dev.overgrown.thaumaturge.mixin;

import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.utils.SpellHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
    @Unique
    private static final Identifier SPELL_CAST_ID = new Identifier("thaumaturge", "spell_cast");

    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void handleSpellCastPacket(CustomPayloadC2SPacket packet, CallbackInfo ci) {
        if (packet.getChannel().equals(SPELL_CAST_ID)) {
            PacketByteBuf data = packet.getData();
            SpellCastPacket spellPacket = new SpellCastPacket(data);

            ServerPlayNetworkHandler handler = (ServerPlayNetworkHandler) (Object) this;
            Hand hand = spellPacket.getHand();
            int spellKey = spellPacket.getSpellKey();

            // Execute on main thread
            Objects.requireNonNull(handler.player.getServer()).execute(() -> {
                SpellHandler.castSpell(handler.player, hand, spellKey);
            });

            ci.cancel();
        }
    }
}