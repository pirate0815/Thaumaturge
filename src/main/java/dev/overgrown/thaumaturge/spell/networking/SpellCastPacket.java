package dev.overgrown.thaumaturge.spell.networking;

import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public final class SpellCastPacket {

    public static final Identifier ID = new Identifier("thaumaturge", "spell_cast");

    public enum Tier { SELF, TARGETED, AOE }

    // target kind tag
    private static final int T_NONE = 0, T_SELF = 1, T_BLOCK = 2, T_ENTITY = 3, T_AOE = 4;

    private final Tier tier;
    private final int tKind;
    private final BlockPos pos;      // BLOCK/AOE
    private final Direction face;    // BLOCK
    private final float radius;      // AOE
    private final int entityId;      // ENTITY

    private SpellCastPacket(Tier tier, int tKind, BlockPos pos, Direction face, float radius, int entityId) {
        this.tier = tier;
        this.tKind = tKind;
        this.pos = pos;
        this.face = face;
        this.radius = radius;
        this.entityId = entityId;
    }

    // ---------- client send helpers ----------
    @Environment(EnvType.CLIENT)
    public static void sendSelf() {
        var mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.getNetworkHandler() == null) return;
        if (!ClientPlayNetworking.canSend(ID)) return;
        send(new SpellCastPacket(Tier.SELF, T_SELF, BlockPos.ORIGIN, null, 0f, 0));
    }

    @Environment(EnvType.CLIENT)
    public static void sendTargetedFromCrosshair() {
        var mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.getNetworkHandler() == null) return;
        if (!ClientPlayNetworking.canSend(ID)) return;

        HitResult hr = mc.crosshairTarget;
        if (hr == null || hr.getType() == HitResult.Type.MISS) return;

        if (hr instanceof BlockHitResult b) {
            send(new SpellCastPacket(Tier.TARGETED, T_BLOCK, b.getBlockPos(), b.getSide(), 0f, 0));
        } else if (hr instanceof EntityHitResult e) {
            Entity ent = e.getEntity();
            if (ent != null) send(new SpellCastPacket(Tier.TARGETED, T_ENTITY, BlockPos.ORIGIN, null, 0f, ent.getId()));
        }
    }

    @Environment(EnvType.CLIENT)
    public static void sendAoeFromCrosshair(float r) {
        var mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.getNetworkHandler() == null) return;
        if (!ClientPlayNetworking.canSend(ID)) return;

        HitResult hr = mc.crosshairTarget;
        BlockPos center = (hr instanceof BlockHitResult b) ? b.getBlockPos() : mc.player.getBlockPos();
        send(new SpellCastPacket(Tier.AOE, T_AOE, center, null, r, 0));
    }

    private static void send(SpellCastPacket pkt) {
        PacketByteBuf out = PacketByteBufs.create();
        pkt.write(out);
        ClientPlayNetworking.send(ID, out);
    }

    // ---------- encode / decode ----------
    private void write(PacketByteBuf buf) {
        buf.writeVarInt(tier.ordinal());
        buf.writeByte(tKind);
        switch (tKind) {
            case T_BLOCK -> {
                buf.writeBlockPos(pos);
                buf.writeEnumConstant(face);
            }
            case T_ENTITY -> buf.writeVarInt(entityId);
            case T_AOE -> {
                buf.writeBlockPos(pos);
                buf.writeFloat(radius);
            }
            default -> { /* T_SELF / T_NONE */ }
        }
    }

    private static SpellCastPacket read(PacketByteBuf buf) {
        int ti = buf.readVarInt();
        Tier tier = (ti >= 0 && ti < Tier.values().length) ? Tier.values()[ti] : Tier.SELF;
        int kind = buf.readByte() & 0xFF;

        return switch (kind) {
            case T_BLOCK -> {
                BlockPos p = buf.readBlockPos();
                Direction f;
                try {
                    f = buf.readEnumConstant(Direction.class);
                } catch (IllegalArgumentException e) {
                    yield new SpellCastPacket(tier, T_NONE, BlockPos.ORIGIN, null, 0f, 0);
                }
                yield new SpellCastPacket(tier, kind, p, f, 0f, 0);
            }
            case T_ENTITY -> new SpellCastPacket(
                    tier, kind,
                    BlockPos.ORIGIN, null, 0f,
                    buf.readVarInt()
            );
            case T_AOE -> {
                BlockPos p = buf.readBlockPos();
                float r = buf.readFloat();
                if (Float.isNaN(r) || r < 0f) r = 0f;
                if (r > 32f) r = 32f;
                yield new SpellCastPacket(tier, kind, p, null, r, 0);
            }
            case T_SELF -> new SpellCastPacket(tier, T_SELF, BlockPos.ORIGIN, null, 0f, 0);
            default -> new SpellCastPacket(tier, T_NONE, BlockPos.ORIGIN, null, 0f, 0);
        };
    }

    // ---------- server registration / handler ----------
    public static void registerServer() {
        ServerPlayNetworking.registerGlobalReceiver(ID, SpellCastPacket::handle);
    }

    private static void handle(MinecraftServer server, ServerPlayerEntity player,
                               ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender response) {
        SpellCastPacket pkt = read(buf);
        server.execute(() -> {
            if (player.isRemoved() || player.isSpectator()) return;

            switch (pkt.tier) {
                case SELF -> {
                    SpellHandler.castSelf(player, new SelfSpellDelivery(player, null, null));
                }
                case TARGETED -> {
                    if (pkt.tKind == T_ENTITY) {
                        var target = player.getWorld().getEntityById(pkt.entityId);
                        if (target != null) {
                            SpellHandler.castTargeted(player, new TargetedSpellDelivery(player, null, null, target));
                        }
                    } else if (pkt.tKind == T_BLOCK && pkt.face != null) {
                        SpellHandler.castTargeted(player, pkt.pos, pkt.face);
                    }
                }
                case AOE -> {
                    if (pkt.tKind == T_AOE) {
                        float radius = Math.max(0f, Math.min(pkt.radius, 32f));
                        SpellHandler.castAoe(player, new AoeSpellDelivery(player, null, null, pkt.pos, radius));
                    }
                }
            }
        });
    }
}
