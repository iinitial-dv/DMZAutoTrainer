package com.iinitial.dmzautotrainer.client.session;

import com.iinitial.dmzautotrainer.common.network.NetworkHandler;
import com.iinitial.dmzautotrainer.common.network.packet.SessionStatusS2CPacket;
import com.iinitial.dmzautotrainer.common.network.packet.TrainingSessionActionC2SPacket;
import com.iinitial.dmzautotrainer.common.network.packet.TrainingSessionActionC2SPacket.Action;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.slf4j.Logger;

@OnlyIn(Dist.CLIENT)
public final class ClientSessionState {
    // DEBUG LOGGING
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG = "[DMZAT-DEBUG]";

    private static boolean awaitingServerResponse;
    private static boolean sessionTimerActive = false;
    private static boolean allowed;
    private static boolean trainingStartSent;
    private static boolean sessionsEnabled = true;
    private static boolean autoTrainerEnabled = false;
    private static long nextRequestAt;
    private static long sessionEndsAt;
    private static long cooldownEndsAt;

    private ClientSessionState() {
    }

    public static boolean mayTrain() {
        if (allowed) {
            return true;
        }

        requestStatusIfDue();
        return false;
    }

    public static boolean isAllowed() {
        return allowed;
    }

    public static boolean isSessionsEnabled() {
        return sessionsEnabled;
    }

    public static boolean isAutoTrainerEnabled() {
        return autoTrainerEnabled;
    }

    public static void updatePolicy(boolean enabled) {
        autoTrainerEnabled = enabled;
    }

    /**
     * Clears every field back to its declared default. Called on disconnect: this class is a
     * static holder, so without it one server's state leaks into the next connection.
     */
    public static void reset() {
        // DEBUG LOGGING
        LOGGER.info("{} reset() called. Was: allowed={}, awaitingServerResponse={}, autoTrainerEnabled={}", TAG, allowed, awaitingServerResponse, autoTrainerEnabled);
        autoTrainerEnabled = false;
        sessionsEnabled = true;
        allowed = false;
        awaitingServerResponse = false;
        trainingStartSent = false;
        sessionTimerActive = false;
        nextRequestAt = 0L;
        sessionEndsAt = 0L;
        cooldownEndsAt = 0L;
    }

    public static boolean isAwaitingResponse() {
        return awaitingServerResponse;
    }

    public static void requestFreshStatus() {
        allowed = false;
        if (awaitingServerResponse) {
            // DEBUG LOGGING
            LOGGER.info("{} requestFreshStatus() SKIPPED sending CHECK - awaitingServerResponse already true.", TAG);
            return;
        }
        // DEBUG LOGGING
        LOGGER.info("{} requestFreshStatus() sending Action.CHECK. Setting awaitingServerResponse=true.", TAG);
        awaitingServerResponse = true;
        send(Action.CHECK);
    }

    public static void endSessionEarly() {
        if (awaitingServerResponse) {
            // DEBUG LOGGING
            LOGGER.info("{} endSessionEarly() SKIPPED sending END - awaitingServerResponse already true.", TAG);
            return;
        }
        // DEBUG LOGGING
        LOGGER.info("{} endSessionEarly() sending Action.END. Setting awaitingServerResponse=true.", TAG);
        allowed = false;
        awaitingServerResponse = true;
        send(Action.END);
    }

    public static void notifyTrainingStarted() {
        if (trainingStartSent) {
            return;
        }
        // DEBUG LOGGING
        LOGGER.info("{} notifyTrainingStarted() sending Action.START (first time this run).", TAG);
        trainingStartSent = true;
        send(Action.START);
    }

    public static void update(SessionStatusS2CPacket status) {
        long now = System.currentTimeMillis();

        // DEBUG LOGGING
        LOGGER.info("{} update() RECEIVED response. allowed={}, sessionsEnabled={}, sessionSecondsRemaining={}, " + "cooldownSecondsRemaining={}. Was awaitingServerResponse={} before this call.", TAG, status.allowed(), status.sessionsEnabled(), status.sessionSecondsRemaining(), status.cooldownSecondsRemaining(), awaitingServerResponse);

        awaitingServerResponse = false;
        allowed = status.allowed();
        sessionsEnabled = status.sessionsEnabled();
        sessionTimerActive = status.sessionSecondsRemaining() > 0L;
        sessionEndsAt = now + status.sessionSecondsRemaining() * 1_000L;
        cooldownEndsAt = now + status.cooldownSecondsRemaining() * 1_000L;
        nextRequestAt = allowed ? 0L : now + 1_000L;

        if (!allowed) {
            trainingStartSent = false;
        }

        // DEBUG LOGGING
        LOGGER.info("{} update() finished. allowed={}, awaitingServerResponse={}, nextRequestAt in {}ms", TAG, allowed, awaitingServerResponse, nextRequestAt - now);
    }

    public static boolean isSessionExpired() {
        return sessionTimerActive && System.currentTimeMillis() >= sessionEndsAt;
    }

    public static long getSessionSecondsRemaining() {
        return secondsRemaining(sessionEndsAt);
    }

    public static long getCooldownSecondsRemaining() {
        return secondsRemaining(cooldownEndsAt);
    }

    public static String formatDuration(long totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public static void syncStatus() {
        if (awaitingServerResponse) {
            return;
        }
        awaitingServerResponse = true;
        send(Action.CHECK);
    }

    private static void requestStatusIfDue() {
        long now = System.currentTimeMillis();
        if (awaitingServerResponse || now < nextRequestAt) {
            // DEBUG LOGGING
            // This is called from mayTrain(), which can be hit every client tick (20x/sec)
            // while blocked, so it's throttled to roughly once per second to avoid flooding
            // the log while still showing whether it's stuck waiting on a response
            // vs. just waiting out the 1-second retry cooldown between requests.
            if (now % 1000 < 50) {
                LOGGER.info("{} requestStatusIfDue() SKIPPED. awaitingServerResponse={}, msUntilNextRequest={}", TAG, awaitingServerResponse, nextRequestAt - now);
            }
            return;
        }

        // DEBUG LOGGING
        LOGGER.info("{} requestStatusIfDue() sending Action.REQUEST. Setting awaitingServerResponse=true.", TAG);
        awaitingServerResponse = true;
        send(Action.REQUEST);
    }

    private static void send(Action action) {
        NetworkHandler.CHANNEL.sendToServer(new TrainingSessionActionC2SPacket(action));
    }

    private static long secondsRemaining(long endsAt) {
        return Math.max(0L, (endsAt - System.currentTimeMillis() + 999L) / 1_000L);
    }
}