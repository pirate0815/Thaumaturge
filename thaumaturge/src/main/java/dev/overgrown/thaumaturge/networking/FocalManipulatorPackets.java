package dev.overgrown.thaumaturge.networking;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.block.focal_manipulator.screen.FocalManipulatorScreenHandler;
import dev.overgrown.thaumaturge.spell.focal.SpellNode;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * C2S packets for the Focal Manipulator.
 */
public final class FocalManipulatorPackets {

    public static final Identifier CRAFT_SPELL = Thaumaturge.identifier("focal_craft");

    private FocalManipulatorPackets() {}

    /**
     * Registers all server-side packet receivers. Call from mod init.
     */
    public static void registerServerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(CRAFT_SPELL, (server, player, handler, buf, responseSender) -> {
            NbtCompound treeNbt = buf.readNbt();
            String spellName = buf.readString(50);

            server.execute(() -> {
                if (player.currentScreenHandler instanceof FocalManipulatorScreenHandler fmHandler) {
                    SpellNode tree = treeNbt != null ? SpellNode.fromNbt(treeNbt) : null;
                    fmHandler.handleCraftRequest(player, tree, spellName);
                }
            });
        });
    }

    /**
     * Client-side: sends a craft request to the server.
     */
    public static PacketByteBuf writeCraftPacket(SpellNode tree, String spellName) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeNbt(tree.toNbt());
        buf.writeString(spellName, 50);
        return buf;
    }
}
