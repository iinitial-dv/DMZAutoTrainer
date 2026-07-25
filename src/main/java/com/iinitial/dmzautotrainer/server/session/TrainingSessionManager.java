package com.iinitial.dmzautotrainer.server.session;

import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.iinitial.dmzautotrainer.common.config.ServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class TrainingSessionManager {
    private TrainingSessionManager() {
    }

    public static SessionStatus requestSession(MinecraftServer server, UUID playerId) {
        ServerConfig config = ConfigManager.server();
        if (!config.isSessionsEnabled()) {
            return allowedWithoutTimer();
        }

        long now = System.currentTimeMillis();
        long sessionDurationMillis = secondsToMillis(config.getSessionDuration());
        long cooldownMillis = secondsToMillis(config.getSessionCooldown());
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);
        PlayerSessionTimes times = data.get(playerId);

        if (times != null && now < times.cooldownEndsAt()) {
            return coolingDown(times.cooldownEndsAt(), now);
        }

        if (times != null && now < times.sessionEndsAt()) {
            return activeSession(times.sessionEndsAt(), now);
        }

        if (times != null && times.sessionEndsAt() > 0L) {
            long cooldownEndsAt = now + cooldownMillis;
            if (now < cooldownEndsAt) {
                data.put(playerId, new PlayerSessionTimes(0L, cooldownEndsAt));
                return coolingDown(cooldownEndsAt, now);
            }
        }

        long sessionEndsAt = now + sessionDurationMillis;
        data.put(playerId, new PlayerSessionTimes(sessionEndsAt, 0L));
        return activeSession(sessionEndsAt, now);
    }

    public static SessionStatus endSessionEarly(MinecraftServer server, UUID playerId) {
        ServerConfig config = ConfigManager.server();
        if (!config.isSessionsEnabled()) {
            return allowedWithoutTimer();
        }

        long now = System.currentTimeMillis();
        long sessionDurationMillis = secondsToMillis(config.getSessionDuration());
        long cooldownMillis = secondsToMillis(config.getSessionCooldown());
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);
        PlayerSessionTimes times = data.get(playerId);

        if (times == null) {
            return requestSession(server, playerId);
        }
        if (now < times.cooldownEndsAt()) {
            return coolingDown(times.cooldownEndsAt(), now);
        }
        if (times.sessionEndsAt() <= 0L) {
            return requestSession(server, playerId);
        }

        long sessionStartedAt = times.sessionEndsAt() - sessionDurationMillis;
        long timeInSession = Math.max(0L, Math.min(now - sessionStartedAt, sessionDurationMillis));
        long proportionalCooldown = Math.round((timeInSession / (double) sessionDurationMillis) * cooldownMillis
        );

        if (proportionalCooldown == 0L) {
            data.remove(playerId);
            return requestSession(server, playerId);
        }

        long cooldownEndsAt = now + proportionalCooldown;
        data.put(playerId, new PlayerSessionTimes(0L, cooldownEndsAt));
        return coolingDown(cooldownEndsAt, now);
    }

    public static boolean resetCooldown(MinecraftServer server, UUID playerId) {
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);
        PlayerSessionTimes times = data.get(playerId);
        if (times == null) {
            return false;
        }

        if (times.sessionEndsAt() > System.currentTimeMillis()) {
            data.put(playerId, new PlayerSessionTimes(times.sessionEndsAt(), 0L));
        } else {
            data.remove(playerId);
        }
        return true;
    }

    public static void setCooldown(MinecraftServer server, UUID playerId, int cooldownSeconds) {
        if (cooldownSeconds <= 0) {
            resetCooldown(server, playerId);
            return;
        }

        long cooldownEndsAt = System.currentTimeMillis() + cooldownSeconds * 1_000L;
        TrainingSessionSavedData.get(server).put(playerId, new PlayerSessionTimes(0L, cooldownEndsAt));
    }

    public static int resetCooldowns(MinecraftServer server) {
        int changed = 0;
        for (UUID playerId : affectedPlayerIds(server)) {
            if (resetCooldown(server, playerId)) {
                changed++;
            }
        }
        return changed;
    }

    public static int setCooldowns(MinecraftServer server, int cooldownSeconds) {
        Set<UUID> playerIds = affectedPlayerIds(server);
        for (UUID playerId : playerIds) {
            setCooldown(server, playerId, cooldownSeconds);
        }
        return playerIds.size();
    }

    public static SessionStatus checkStatus(MinecraftServer server, UUID playerId) {
        ServerConfig config = ConfigManager.server();
        if (!config.isSessionsEnabled()) {
            return new SessionStatus(true, 0L, 0L);
        }

        long now = System.currentTimeMillis();
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);
        PlayerSessionTimes times = data.get(playerId);

        if (times == null) {
            return new SessionStatus(false, 0L, 0L);
        }
        if (now < times.cooldownEndsAt()) {
            return new SessionStatus(false, 0L, Math.max(0L, (times.cooldownEndsAt() - now + 999L) / 1000L));
        }
        if (now < times.sessionEndsAt()) {
            return new SessionStatus(true, Math.max(0L, (times.sessionEndsAt() - now + 999L) / 1000L), 0L);
        }
        return new SessionStatus(false, 0L, 0L);
    }

    private static SessionStatus allowedWithoutTimer() {
        return new SessionStatus(true, 0L, 0L);
    }

    private static SessionStatus activeSession(long sessionEndsAt, long now) {
        return new SessionStatus(true, secondsRemaining(sessionEndsAt, now), 0L);
    }

    private static SessionStatus coolingDown(long cooldownEndsAt, long now) {
        return new SessionStatus(false, 0L, secondsRemaining(cooldownEndsAt, now));
    }

    private static long secondsToMillis(int seconds) {
        return Math.max(1L, seconds) * 1_000L;
    }

    private static long secondsRemaining(long endsAt, long now) {
        return Math.max(0L, (endsAt - now + 999L) / 1_000L);
    }

    private static Set<UUID> affectedPlayerIds(MinecraftServer server) {
        Set<UUID> playerIds = new HashSet<>(TrainingSessionSavedData.get(server).playerIds());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            playerIds.add(player.getUUID());
        }
        return playerIds;
    }
}
