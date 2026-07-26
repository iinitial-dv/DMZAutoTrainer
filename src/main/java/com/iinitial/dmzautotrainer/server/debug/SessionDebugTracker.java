package com.iinitial.dmzautotrainer.server.debug;

import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.iinitial.dmzautotrainer.common.config.ServerConfig;
import com.iinitial.dmzautotrainer.server.session.PlayerSessionTimes;
import com.iinitial.dmzautotrainer.server.session.TrainingSessionSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionDebugTracker {
    private static final Map<UUID, UUID> watchers = new ConcurrentHashMap<>();

    private SessionDebugTracker() {
    }

    public static boolean toggle(ServerPlayer watcher, UUID target) {
        UUID current = watchers.get(watcher.getUUID());
        if (current != null && current.equals(target)) {
            watchers.remove(watcher.getUUID());
            return false;
        }
        watchers.put(watcher.getUUID(), target);
        sendSnapshot(watcher, watcher.server, target);
        return true;
    }

    public static void tick(MinecraftServer server) {
        if (watchers.isEmpty() || server.getTickCount() % 20 != 0) {
            return;
        }

        watchers.entrySet().removeIf(entry -> {
            ServerPlayer watcher = server.getPlayerList().getPlayer(entry.getKey());
            if (watcher == null) {
                return true;
            }
            sendSnapshot(watcher, server, entry.getValue());
            return false;
        });
    }

    private static void sendSnapshot(ServerPlayer watcher, MinecraftServer server, UUID targetId) {
        String targetName = resolveName(server, targetId);
        ServerConfig config = ConfigManager.server();
        PlayerSessionTimes times = TrainingSessionSavedData.get(server).get(targetId);
        long now = System.currentTimeMillis();

        watcher.sendSystemMessage(Component.literal("--- DMZ Trainer Debug: " + targetName + " ---")
                .withStyle(ChatFormatting.GOLD));
        watcher.sendSystemMessage(line("now", String.valueOf(now)));
        watcher.sendSystemMessage(line("state", describeState(times, now)));
        watcher.sendSystemMessage(line("sessionsEnabled", String.valueOf(config.isSessionsEnabled())));
        watcher.sendSystemMessage(line("sessionDuration", config.getSessionDuration() + "s"));
        watcher.sendSystemMessage(line("sessionCooldown", config.getSessionCooldown() + "s"));

        if (times == null) {
            watcher.sendSystemMessage(line("data", "none on file (idle, never trained)"));
            return;
        }

        watcher.sendSystemMessage(line("sessionGrantedAt", describeTimestamp(times.sessionGrantedAt(), now)));
        watcher.sendSystemMessage(line("sessionEndsAt", describeTimestamp(times.sessionEndsAt(), now)));
        watcher.sendSystemMessage(line("cooldownEndsAt", describeTimestamp(times.cooldownEndsAt(), now)));
    }

    private static Component line(String key, String value) {
        return Component.literal(" " + key + " = ").withStyle(ChatFormatting.GRAY)
                .append(Component.literal(value).withStyle(ChatFormatting.WHITE));
    }

    private static String describeState(PlayerSessionTimes times, long now) {
        if (times == null) {
            return "IDLE";
        }
        if (now < times.cooldownEndsAt()) {
            return "COOLDOWN";
        }
        if (times.sessionEndsAt() > 0L && now < times.sessionEndsAt()) {
            return "ACTIVE";
        }
        if (times.sessionEndsAt() > 0L) {
            return "ACTIVE (expired, not yet formally closed)";
        }
        if (times.sessionGrantedAt() > 0L) {
            return "GRANTED (awaiting start)";
        }
        return "IDLE";
    }

    private static String describeTimestamp(long value, long now) {
        if (value <= 0L) {
            return "0 (unset)";
        }
        long deltaMillis = value - now;
        String relative = deltaMillis >= 0
                ? String.format("in %.1fs", deltaMillis / 1000.0)
                : String.format("%.1fs ago", -deltaMillis / 1000.0);
        return value + " (" + relative + ")";
    }

    private static String resolveName(MinecraftServer server, UUID uuid) {
        ServerPlayer online = server.getPlayerList().getPlayer(uuid);
        if (online != null) {
            return online.getGameProfile().getName();
        }
        if (server.getProfileCache() != null) {
            return server.getProfileCache().get(uuid)
                    .map(com.mojang.authlib.GameProfile::getName)
                    .orElse(uuid.toString());
        }
        return uuid.toString();
    }
}