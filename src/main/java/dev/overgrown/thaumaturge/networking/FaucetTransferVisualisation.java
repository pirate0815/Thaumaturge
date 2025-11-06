package dev.overgrown.thaumaturge.networking;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class FaucetTransferVisualisation {

    public static final Identifier ASPECT_TRANSFER_PAKET = Thaumaturge.identifier("aspect_transfer");

    public static void sendToPlayer(ServerWorld world, BlockPos start, BlockPos end) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(start);
        buf.writeBlockPos(end);
        world.getPlayers(player -> player.getBlockPos().isWithinDistance(start, 64))
                .forEach(player -> ServerPlayNetworking.send(player, ASPECT_TRANSFER_PAKET, buf));
    }


}
