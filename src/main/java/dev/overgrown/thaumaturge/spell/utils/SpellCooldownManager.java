package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket.KeyType;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpellCooldownManager {
    private static final Map<UUID, Map<KeyType, Integer>> cooldowns = new HashMap<>();
    private static final int COOLDOWN_TICKS = 13;

    public static boolean isOnCooldown(ServerPlayerEntity player, KeyType keyType) {
        UUID playerId = player.getUuid();
        if (!cooldowns.containsKey(playerId)) return false;

        Map<KeyType, Integer> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(keyType)) return false;

        return playerCooldowns.get(keyType) > 0;
    }

    public static void setCooldown(ServerPlayerEntity player, KeyType keyType) {
        UUID playerId = player.getUuid();
        if (!cooldowns.containsKey(playerId)) {
            cooldowns.put(playerId, new HashMap<>());
        }

        Map<KeyType, Integer> playerCooldowns = cooldowns.get(playerId);
        playerCooldowns.put(keyType, COOLDOWN_TICKS);
    }

    public static void tick() {
        for (Map<KeyType, Integer> playerCooldowns : cooldowns.values()) {
            for (KeyType keyType : KeyType.values()) {
                if (playerCooldowns.containsKey(keyType)) {
                    int remaining = playerCooldowns.get(keyType);
                    if (remaining > 0) {
                        playerCooldowns.put(keyType, remaining - 1);
                    }
                }
            }
        }
    }

    public static void removePlayer(UUID playerId) {
        cooldowns.remove(playerId);
    }

    public static float getCooldownProgress(ServerPlayerEntity player, KeyType keyType) {
        UUID playerId = player.getUuid();
        if (!cooldowns.containsKey(playerId)) return 0.0f;

        Map<KeyType, Integer> playerCooldowns = cooldowns.get(playerId);
        if (!playerCooldowns.containsKey(keyType)) return 0.0f;

        int remaining = playerCooldowns.get(keyType);
        return Math.max(0.0f, Math.min(1.0f, (float) remaining / COOLDOWN_TICKS));
    }
}