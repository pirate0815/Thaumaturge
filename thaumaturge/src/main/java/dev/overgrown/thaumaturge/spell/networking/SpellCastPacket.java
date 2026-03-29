package dev.overgrown.thaumaturge.spell.networking;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import dev.overgrown.thaumaturge.spell.utils.SpellCooldownManager;
import dev.overgrown.thaumaturge.spell.utils.SpellHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public final class SpellCastPacket {

    public static final Identifier ID = Thaumaturge.identifier("spell_cast");

    public enum KeyType { PRIMARY, SECONDARY, TERNARY }

    @Environment(EnvType.CLIENT)
    public static void send(KeyType keyType) {
        var mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!ClientPlayNetworking.canSend(ID)) return;

        PacketByteBuf out = PacketByteBufs.create();
        out.writeEnumConstant(keyType);
        ClientPlayNetworking.send(ID, out);
    }

    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ID, SpellCastPacket::handle);
    }

    private static void handle(MinecraftServer server, ServerPlayerEntity player,
                               ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender response) {
        KeyType keyType = buf.readEnumConstant(KeyType.class);
        server.execute(() -> {
            if (player.isRemoved() || player.isSpectator()) return;

            // Check if spell is on cooldown
            if (SpellCooldownManager.isOnCooldown(player, keyType)) {
                return;
            }

            boolean spellCast = false;

            switch (keyType) {
                case PRIMARY -> {
                    SelfSpellDelivery delivery = new SelfSpellDelivery(player);
                    spellCast = SpellHandler.cast(player, delivery, "primary");
                }
                case SECONDARY -> {
                    Vec3d start = player.getCameraPosVec(0);
                    Vec3d end = start.add(player.getRotationVec(0).multiply(16));

                    // First check for entities in the ray path
                    EntityHitResult entityHit = getEntityHit(player, start, end);
                    if (entityHit != null) {
                        TargetedSpellDelivery delivery = new TargetedSpellDelivery(player, entityHit.getEntity());
                        spellCast = SpellHandler.cast(player, delivery, "secondary");
                    }
                    // Then check for blocks if no entity was hit
                    else {
                        HitResult hit = player.raycast(16, 0, false);
                        if (hit.getType() == HitResult.Type.BLOCK) {
                            BlockHitResult bHit = (BlockHitResult) hit;
                            TargetedSpellDelivery delivery = new TargetedSpellDelivery(player, bHit.getBlockPos(), bHit.getSide());
                            spellCast = SpellHandler.cast(player, delivery, "secondary");
                        }
                    }
                }
                case TERNARY -> {
                    AoeSpellDelivery delivery = new AoeSpellDelivery(player, player.getBlockPos(), 3.0f);
                    spellCast = SpellHandler.cast(player, delivery, "ternary");
                }
            }

            // Only set cooldown if spell was successfully cast
            if (spellCast) {
                SpellCooldownManager.setCooldown(player, keyType);
            }
        });
    }

    private static EntityHitResult getEntityHit(ServerPlayerEntity player, Vec3d start, Vec3d end) {
        // Create a bounding box along the ray path
        Box box = player.getBoundingBox().stretch(player.getRotationVec(0).multiply(16)).expand(1.0, 1.0, 1.0);

        // Find the closest entity in the ray path
        Entity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : player.getWorld().getOtherEntities(player, box,
                e -> e != null && e.isAlive() && e.canHit() && !e.isSpectator())) {

            Box entityBox = entity.getBoundingBox().expand(0.3);
            Optional<Vec3d> hitPos = entityBox.raycast(start, end);

            if (hitPos.isPresent()) {
                double distance = start.squaredDistanceTo(hitPos.get());
                if (distance < closestDistance) {
                    closest = entity;
                    closestDistance = distance;
                }
            }
        }

        return closest != null ? new EntityHitResult(closest) : null;
    }
}