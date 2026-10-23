package com.iinitial.dmzautotrainer.client.autotrainer;

import com.dragonminez.client.gui.character.minigames.BaseMinigameScreen;
import com.dragonminez.client.gui.character.minigames.RythmGameScreen;
import com.dragonminez.common.training.GravityLogic;
import com.dragonminez.common.training.KiControlLogic;
import com.dragonminez.common.training.MemoryLogic;
import com.dragonminez.common.training.MinigameLogic;
import com.dragonminez.common.training.PrecisionLogic;
import com.dragonminez.common.training.RhythmLogic;
import com.iinitial.dmzautotrainer.client.minigames.*;
import com.iinitial.dmzautotrainer.client.session.ClientSessionState;
import com.iinitial.dmzautotrainer.common.config.ClientConfig;
import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class AutoTrainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RETRY_TAG = "[DMZ Auto Trainer]";

    private static final long PENDING_RESTART_RESPONSE_TIMEOUT_MS = 3000L;
    private static final long PENDING_RESTART_RETRY_DELAY_MS = 3000L;
    private static final int MAX_CONSECUTIVE_REJECTS = 5;

    private static Class<? extends BaseMinigameScreen> repeatingScreenClass = null;
    private static BaseMinigameScreen evaluatedScreen = null;
    private static ScreenInputSink sink = null;
    private static boolean repeating = false;
    private static boolean wasAutoTrainerEnabled = false;
    private static boolean pendingRestart = false;
    private static boolean sessionExpiredThisRun = false;
    private static boolean evaluationPending = false;
    private static boolean automatingCurrentScreen = false;
    private static boolean skipCurrentScreen = false;
    private static boolean endRequested = false;
    private static boolean rejectHandled = false;
    private static boolean finishHandled = false;
    private static boolean automatedEnd = false;
    private static boolean userStopped = false;
    private static boolean autoStartReady = false;
    private static long pendingRestartAttemptDeadline = 0L;
    private static long pendingRestartRetryAt = 0L;
    private static int pendingRestartCountdownLogged = -1;
    private static int consecutiveRejects = 0;

    public static boolean isAutomatedEnd() {
        return automatedEnd;
    }

    public static void onUserStopped() {
        userStopped = true;
        repeating = false;
        clearPendingRestart();
    }

    public static void globalTick(Minecraft mc) {
        if (!ClientSessionState.isAutoTrainerEnabled()) {
            resetState();
            return;
        }

        ClientConfig config = ConfigManager.client();
        if (!config.isAutoTrainEnabled()) {
            if (wasAutoTrainerEnabled) {
                ClientSessionState.endSessionEarly();
            }
            resetState();
            return;
        }
        wasAutoTrainerEnabled = true;

        if (mc.screen instanceof BaseMinigameScreen screen) {
            if (screen != evaluatedScreen) {
                adopt(screen, false);
                skipCurrentScreen = isChallenge(screen);
                if (!skipCurrentScreen) {
                    evaluationPending = true;
                    ClientSessionState.mayTrain();
                }
            }
            if (skipCurrentScreen) {
                return;
            }

            if (evaluationPending) {
                if (ClientSessionState.isAwaitingResponse()) {
                    return;
                }
                evaluationPending = false;
                automatingCurrentScreen = ClientSessionState.isAllowed();
            }

            if (automatingCurrentScreen) {
                tick(screen, config);
            } else {
                ClientSessionState.mayTrain();
            }
        } else {
            evaluatedScreen = null;
            evaluationPending = false;
            automatingCurrentScreen = false;
            skipCurrentScreen = false;
            sink = null;

            if (sessionExpiredThisRun) {
                sessionExpiredThisRun = false;
                clearPendingRestart();
                repeating = false;
                ClientSessionState.endSessionEarly();
            } else if (pendingRestart) {
                handlePendingRestart(mc);
            }
        }
    }

    private static void adopt(BaseMinigameScreen screen, boolean automating) {
        userStopped = false;
        autoStartReady = automating;
        evaluatedScreen = screen;
        evaluationPending = false;
        automatingCurrentScreen = automating;
        skipCurrentScreen = false;
        endRequested = false;
        rejectHandled = false;
        finishHandled = false;
        sink = new ScreenInputSink(screen);
    }

    private static boolean isChallenge(BaseMinigameScreen screen) {
        return Reflect.get(screen, "challenge") != null;
    }

    private static void handlePendingRestart(Minecraft mc) {
        long now = System.currentTimeMillis();

        if (pendingRestartRetryAt > 0L) {
            if (now < pendingRestartRetryAt) {
                logCountdown(now);
                return;
            }
            // Countdown finished
            pendingRestartRetryAt = 0L;
            pendingRestartCountdownLogged = -1;
            pendingRestartAttemptDeadline = now + PENDING_RESTART_RESPONSE_TIMEOUT_MS;
            ClientSessionState.requestFreshStatus();
            return;
        }

        if (ClientSessionState.isAwaitingResponse()) {
            if (now < pendingRestartAttemptDeadline) {
                return; // still waiting on this attempt
            }
            beginRetryCountdown(now, true);
            return;
        }

        if (ClientSessionState.isAllowed()) {
            LOGGER.info("{} Success.", RETRY_TAG);
            pendingRestart = false;
            repeating = true;

            try {
                BaseMinigameScreen fresh = repeatingScreenClass.getDeclaredConstructor().newInstance();
                mc.setScreen(fresh);
                adopt(fresh, true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to restart minigame for repeat training", e);
            }
            return;
        }

        beginRetryCountdown(now, false);
    }

    private static void beginRetryCountdown(long now, boolean dueToTimeout) {
        pendingRestartRetryAt = now + PENDING_RESTART_RETRY_DELAY_MS;
        int seconds = (int) (PENDING_RESTART_RETRY_DELAY_MS / 1000L);
        pendingRestartCountdownLogged = seconds;

        if (dueToTimeout) {
            LOGGER.info("{} No response from server. Trying again in {}...", RETRY_TAG, seconds);
        } else {
            LOGGER.info("{} Server denied restart. Trying again in {}...", RETRY_TAG, seconds);
        }
    }

    private static void logCountdown(long now) {
        int secondsLeft = (int) Math.ceil((pendingRestartRetryAt - now) / 1000.0);
        if (secondsLeft > 0 && secondsLeft != pendingRestartCountdownLogged) {
            pendingRestartCountdownLogged = secondsLeft;
            LOGGER.info("{} {}...", RETRY_TAG, secondsLeft);
        }
    }

    private static void clearPendingRestart() {
        pendingRestart = false;
        pendingRestartAttemptDeadline = 0L;
        pendingRestartRetryAt = 0L;
        pendingRestartCountdownLogged = -1;
    }

    private static void tick(BaseMinigameScreen screen, ClientConfig config) {
        String stage = ((Enum<?>) Reflect.get(screen, "stage")).name();

        switch (stage) {
            case "READY" -> {
                if (autoStartReady || !(screen instanceof RythmGameScreen)) {
                    press(screen, GLFW.GLFW_KEY_ENTER);
                }
            }
            case "FINISHED" -> {
                onFinished(screen, config);
                press(screen, GLFW.GLFW_KEY_ENTER);
            }
            case "REJECTED" -> onRejected(screen, config);
            case "PLAYING" -> playing(screen, config);
            default -> { }
        }
    }

    private static void onFinished(BaseMinigameScreen screen, ClientConfig config) {
        if (finishHandled) {
            return;
        }
        finishHandled = true;

        boolean restart = config.isRepeatTrainingEnabled() && !endRequested && !userStopped && !sessionExpiredThisRun;
        if (restart) {
            repeatingScreenClass = screen.getClass();
            repeating = true;
            pendingRestart = true;
            pendingRestartAttemptDeadline = System.currentTimeMillis() + PENDING_RESTART_RESPONSE_TIMEOUT_MS;
            pendingRestartRetryAt = 0L;
            pendingRestartCountdownLogged = -1;
            ClientSessionState.requestFreshStatus();
        } else if (!endRequested) {
            repeating = false;
            clearPendingRestart();
        }
    }

    private static void playing(BaseMinigameScreen screen, ClientConfig config) {
        ClientSessionState.notifyTrainingStarted();
        consecutiveRejects = 0;

        MinigameLogic logic = (MinigameLogic) Reflect.get(screen, "logic");
        if (logic == null || logic.isFinished()) {
            return;
        }

        if (ClientSessionState.isSessionExpired()) {
            sessionExpiredThisRun = true;
            if (!isSong(logic) && !endRequested) {
                clearPendingRestart();
                repeating = false;
                endRequested = true;
                automatedEnd = true;
                try {
                    Reflect.invoke(screen, "endByPlayer");
                } finally {
                    automatedEnd = false;
                }
                return;
            }
        }

        try {
            sink.attach(logic);
            dispatch(logic);
        } catch (RuntimeException e) {
            LOGGER.error("{} Automation failed", RETRY_TAG, e);
            automatingCurrentScreen = false;
        }
    }

    private static boolean isSong(MinigameLogic logic) {
        return logic instanceof RhythmLogic rhythm && rhythm.chart() != null;
    }

    private static void onRejected(BaseMinigameScreen screen, ClientConfig config) {
        if (!rejectHandled) {
            rejectHandled = true;
            if (repeating && config.isRepeatTrainingEnabled() && ++consecutiveRejects <= MAX_CONSECUTIVE_REJECTS) {
                pendingRestart = true;
                pendingRestartAttemptDeadline = 0L;
                pendingRestartRetryAt = System.currentTimeMillis() + PENDING_RESTART_RETRY_DELAY_MS;
                pendingRestartCountdownLogged = -1;
            } else {
                repeating = false;
                clearPendingRestart();
            }
        }
        press(screen, GLFW.GLFW_KEY_ENTER);
    }

    private static void press(BaseMinigameScreen screen, int keyCode) {
        screen.keyPressed(keyCode, 0, 0);
        screen.keyReleased(keyCode, 0, 0);
    }

    private static void dispatch(MinigameLogic logic) {
        if (logic instanceof RhythmLogic r) {
            RhythmAutomation.tick(r, sink);
        } else if (logic instanceof GravityLogic g) {
            GravityAutomation.tick(g, sink);
        } else if (logic instanceof PrecisionLogic p) {
            PrecisionAutomation.tick(p, sink);
        } else if (logic instanceof MemoryLogic m) {
            MemoryAutomation.tick(m, sink);
        } else if (logic instanceof KiControlLogic c) {
            ControlAutomation.tick(c, sink);
        }
    }

    private static void resetState() {
        wasAutoTrainerEnabled = false;
        repeating = false;
        clearPendingRestart();
        sessionExpiredThisRun = false;
        evaluatedScreen = null;
        evaluationPending = false;
        automatingCurrentScreen = false;
        skipCurrentScreen = false;
        endRequested = false;
        rejectHandled = false;
        finishHandled = false;
        userStopped = false;
        autoStartReady = false;
        consecutiveRejects = 0;
        sink = null;
    }
}