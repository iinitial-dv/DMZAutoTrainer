package com.iinitial.dmzautotrainer.server.session;

import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.iinitial.dmzautotrainer.common.config.ServerConfig;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

public final class TrainingSessionManager {
    private TrainingSessionManager() {
    }

    /**
     * Starts a session when the player has none, or returns the current session/cooldown state.
     */
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
            long cooldownEndsAt = times.sessionEndsAt() + cooldownMillis;
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
        if (now >= times.sessionEndsAt()) {
            return requestSession(server, playerId);
        }

        long sessionStartedAt = times.sessionEndsAt() - sessionDurationMillis;
        long timeInSession = Math.max(0L, Math.min(now - sessionStartedAt, sessionDurationMillis));
        long proportionalCooldown = Math.round(
                (timeInSession / (double) sessionDurationMillis) * cooldownMillis
        );

        if (proportionalCooldown == 0L) {
            data.remove(playerId);
            return requestSession(server, playerId);
        }

        long cooldownEndsAt = now + proportionalCooldown;
        data.put(playerId, new PlayerSessionTimes(0L, cooldownEndsAt));
        return coolingDown(cooldownEndsAt, now);
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
}
