package com.iinitial.dmzautotrainer.client.session;

import com.iinitial.dmzautotrainer.common.network.NetworkHandler;
import com.iinitial.dmzautotrainer.common.network.packet.EndTrainingSessionC2SPacket;
import com.iinitial.dmzautotrainer.common.network.packet.RequestTrainingSessionC2SPacket;
import com.iinitial.dmzautotrainer.common.network.packet.SessionStatusS2CPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class ClientSessionState {
    private static boolean awaitingServerResponse;
    private static boolean allowed;
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

    public static void requestFreshStatus() {
        allowed = false;
        nextRequestAt = 0L;
        requestStatusIfDue();
    }

    public static void endSessionEarly() {
        if (!allowed && !awaitingServerResponse) {
            return;
        }

        allowed = false;
        awaitingServerResponse = true;
        NetworkHandler.CHANNEL.sendToServer(new EndTrainingSessionC2SPacket());
    }

    public static void update(SessionStatusS2CPacket status) {
        long now = System.currentTimeMillis();
        awaitingServerResponse = false;
        allowed = status.allowed();
        sessionEndsAt = now + status.sessionSecondsRemaining() * 1_000L;
        cooldownEndsAt = now + status.cooldownSecondsRemaining() * 1_000L;
        nextRequestAt = allowed ? 0L : now + 1_000L;
    }

    public static long getSessionSecondsRemaining() {
        return secondsRemaining(sessionEndsAt);
    }

    public static long getCooldownSecondsRemaining() {
        return secondsRemaining(cooldownEndsAt);
    }

    public static String formatDuration(long seconds) {
        return String.format("%d:%02d", seconds / 60L, seconds % 60L);
    }

    private static void requestStatusIfDue() {
        long now = System.currentTimeMillis();
        if (awaitingServerResponse || now < nextRequestAt) {
            return;
        }

        awaitingServerResponse = true;
        NetworkHandler.CHANNEL.sendToServer(new RequestTrainingSessionC2SPacket());
    }

    private static long secondsRemaining(long endsAt) {
        return Math.max(0L, (endsAt - System.currentTimeMillis() + 999L) / 1_000L);
    }
}
