package com.iinitial.dmzautotrainer.client.session;

import com.iinitial.dmzautotrainer.common.network.NetworkHandler;
import com.iinitial.dmzautotrainer.common.network.packet.SessionStatusS2CPacket;
import com.iinitial.dmzautotrainer.common.network.packet.TrainingSessionActionC2SPacket;
import com.iinitial.dmzautotrainer.common.network.packet.TrainingSessionActionC2SPacket.Action;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientSessionState {
    private static boolean awaitingServerResponse;
    private static boolean sessionTimerActive = false;
    private static boolean allowed;
    private static boolean trainingStartSent;
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

    public static boolean isAwaitingResponse() {
        return awaitingServerResponse;
    }

    public static void requestFreshStatus() {
        allowed = false;
        if (awaitingServerResponse) {
            return;
        }
        awaitingServerResponse = true;
        send(Action.CHECK);
    }

    public static void endSessionEarly() {
        if (awaitingServerResponse) {
            return;
        }
        allowed = false;
        awaitingServerResponse = true;
        send(Action.END);
    }

    public static void notifyTrainingStarted() {
        if (trainingStartSent) {
            return;
        }
        trainingStartSent = true;
        send(Action.START);
    }

    public static void update(SessionStatusS2CPacket status) {
        long now = System.currentTimeMillis();
        awaitingServerResponse = false;
        allowed = status.allowed();
        sessionTimerActive = status.sessionSecondsRemaining() > 0L;
        sessionEndsAt = now + status.sessionSecondsRemaining() * 1_000L;
        cooldownEndsAt = now + status.cooldownSecondsRemaining() * 1_000L;
        nextRequestAt = allowed ? 0L : now + 1_000L;

        if (!allowed) {
            trainingStartSent = false;
        }
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
            return;
        }

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