package com.iinitial.dmzautotrainer.server.session;

import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.iinitial.dmzautotrainer.common.config.ServerConfig;
import net.minecraft.server.MinecraftServer;

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
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);
        PlayerSessionTimes times = data.get(playerId);

        if (times != null && now < times.cooldownEndsAt()) {
            return coolingDown(times.cooldownEndsAt(), now);
        }

        if (times != null && times.sessionEndsAt() > 0L) {
            if (now < times.sessionEndsAt()) {
                return activeSession(times.sessionEndsAt(), now);
            }
            return closeSession(server, playerId, times, config, now);
        }

        if (times != null && times.sessionGrantedAt() > 0L) {
            return new SessionStatus(true, 0L, 0L, true);
        }

        data.put(playerId, new PlayerSessionTimes(now, 0L, 0L));
        return new SessionStatus(true, 0L, 0L, true);
    }

    public static SessionStatus startSession(MinecraftServer server, UUID playerId) {
        ServerConfig config = ConfigManager.server();
        if (!config.isSessionsEnabled()) {
            return allowedWithoutTimer();
        }

        long now = System.currentTimeMillis();
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);
        PlayerSessionTimes times = data.get(playerId);

        if (times == null || times.sessionGrantedAt() <= 0L) {
            return checkStatus(server, playerId);
        }

        if (times.sessionEndsAt() > 0L) {
            return activeSession(times.sessionEndsAt(), now);
        }

        long sessionEndsAt = now + secondsToMillis(config.getSessionDuration());
        data.put(playerId, new PlayerSessionTimes(times.sessionGrantedAt(), sessionEndsAt, 0L));
        return activeSession(sessionEndsAt, now);
    }

    public static SessionStatus endSessionEarly(MinecraftServer server, UUID playerId) {
        ServerConfig config = ConfigManager.server();
        if (!config.isSessionsEnabled()) {
            return allowedWithoutTimer();
        }

        long now = System.currentTimeMillis();
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);
        PlayerSessionTimes times = data.get(playerId);

        if (times == null) {
            return new SessionStatus(false, 0L, 0L, true);
        }
        if (now < times.cooldownEndsAt()) {
            return coolingDown(times.cooldownEndsAt(), now);
        }

        return closeSession(server, playerId, times, config, now);
    }

    public static SessionStatus checkStatus(MinecraftServer server, UUID playerId) {
        ServerConfig config = ConfigManager.server();
        if (!config.isSessionsEnabled()) {
            return new SessionStatus(true, 0L, 0L, false);
        }

        long now = System.currentTimeMillis();
        PlayerSessionTimes times = TrainingSessionSavedData.get(server).get(playerId);

        if (times == null) {
            return new SessionStatus(false, 0L, 0L, true);
        }
        if (now < times.cooldownEndsAt()) {
            return new SessionStatus(false, 0L, secondsRemaining(times.cooldownEndsAt(), now), true);
        }
        if (times.sessionEndsAt() > 0L && now < times.sessionEndsAt()) {
            return new SessionStatus(true, secondsRemaining(times.sessionEndsAt(), now), 0L, true);
        }
        if (times.sessionGrantedAt() > 0L && times.sessionEndsAt() == 0L) {
            return new SessionStatus(true, 0L, 0L, true);
        }
        return new SessionStatus(false, 0L, 0L, true);
    }

    public static boolean resetCooldown(MinecraftServer server, UUID playerId) {
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);
        PlayerSessionTimes times = data.get(playerId);
        if (times == null) {
            return false;
        }

        if (times.sessionGrantedAt() > 0L || times.sessionEndsAt() > System.currentTimeMillis()) {
            data.put(playerId, new PlayerSessionTimes(times.sessionGrantedAt(), times.sessionEndsAt(), 0L));
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
        TrainingSessionSavedData.get(server).put(playerId, new PlayerSessionTimes(0L, 0L, cooldownEndsAt));
    }

    private static SessionStatus closeSession(MinecraftServer server, UUID playerId, PlayerSessionTimes times, ServerConfig config, long now) {
        TrainingSessionSavedData data = TrainingSessionSavedData.get(server);

        if (times.sessionEndsAt() <= 0L) {
            data.remove(playerId);
            return new SessionStatus(false, 0L, 0L, true);
        }

        long sessionDurationMillis = secondsToMillis(config.getSessionDuration());
        long cooldownMillis = secondsToMillis(config.getSessionCooldown());

        long sessionStartedAt = times.sessionEndsAt() - sessionDurationMillis;
        long timeInSession = Math.max(0L, Math.min(now - sessionStartedAt, sessionDurationMillis));
        long proportionalCooldown = Math.round((timeInSession / (double) sessionDurationMillis) * cooldownMillis);

        if (proportionalCooldown <= 0L) {
            data.remove(playerId);
            return new SessionStatus(false, 0L, 0L, true);
        }

        long cooldownEndsAt = now + proportionalCooldown;
        data.put(playerId, new PlayerSessionTimes(0L, 0L, cooldownEndsAt));
        return coolingDown(cooldownEndsAt, now);
    }

    private static SessionStatus allowedWithoutTimer() {
        return new SessionStatus(true, 0L, 0L, false);
    }

    private static SessionStatus activeSession(long sessionEndsAt, long now) {
        return new SessionStatus(true, secondsRemaining(sessionEndsAt, now), 0L, true);
    }

    private static SessionStatus coolingDown(long cooldownEndsAt, long now) {
        return new SessionStatus(false, 0L, secondsRemaining(cooldownEndsAt, now), true);
    }

    private static long secondsToMillis(int seconds) {
        return Math.max(1L, seconds) * 1_000L;
    }

    private static long secondsRemaining(long endsAt, long now) {
        return Math.max(0L, (endsAt - now + 999L) / 1_000L);
    }
}