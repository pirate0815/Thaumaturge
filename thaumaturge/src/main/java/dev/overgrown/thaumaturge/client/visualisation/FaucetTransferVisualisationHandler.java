package dev.overgrown.thaumaturge.client.visualisation;

import dev.overgrown.thaumaturge.block.faucet.FaucetBlock;
import dev.overgrown.thaumaturge.registry.ModBlocks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class FaucetTransferVisualisationHandler {

    public static void receive(MinecraftClient client, ClientPlayNetworkHandler clientPlayNetworkHandler, PacketByteBuf packetByteBuf, PacketSender packetSender) {
        BlockPos start = packetByteBuf.readBlockPos();
        BlockPos end = packetByteBuf.readBlockPos();
        client.execute(() -> {

           if (client.world != null) {

               Vec3d startPoint;
               BlockState startBlockState = client.world.getBlockState(start);
               if (startBlockState.getBlock() == ModBlocks.FAUCET) {
                   startPoint = FaucetBlock.nozzlePos(start, startBlockState);
               } else {
                   startPoint = new Vec3d(start.getX() + 0.5, start.getY() + 0.5, start.getZ() + 0.5);
               }

               Vec3d endPoint = new Vec3d(end.getX() + 0.5, end.getY() + 0.5, end.getZ() + 0.5);
               Vec3d vector = endPoint.subtract(startPoint).normalize().multiply(0.3333);
               Vec3d current = startPoint;
               long steps = Math.round (startPoint.distanceTo(endPoint) * 3.0);
               for (int i = 0; i < steps; i++) {
                   client.world.addParticle(ParticleTypes.BUBBLE_POP, true, current.x, current.y, current.z, vector.x, vector.y, vector.z);
                   current = current.add(vector);
               }

           }
        });
    }


}
