package com.iinitial.dmzautotrainer.client.autotrainer;

import com.dragonminez.client.gui.character.minigames.*;
import com.iinitial.dmzautotrainer.client.minigames.*;
import com.iinitial.dmzautotrainer.client.session.ClientSessionState;
import com.iinitial.dmzautotrainer.common.config.ClientConfig;
import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

public class AutoTrainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RETRY_TAG = "[DMZ Auto Trainer]";

    private static final long PENDING_RESTART_RESPONSE_TIMEOUT_MS = 3000L;
    private static final long PENDING_RESTART_RETRY_DELAY_MS = 3000L;

    private static boolean repeating = false;
    private static boolean wasAutoTrainerEnabled = false;
    private static boolean pendingRestart = false;
    private static long pendingRestartAttemptDeadline = 0L;
    private static long pendingRestartRetryAt = 0L;
    private static int pendingRestartCountdownLogged = -1;
    private static boolean sessionExpiredThisRun = false;
    private static Class<? extends BaseMinigameScreen> repeatingScreenClass = null;
    private static BaseMinigameScreen evaluatedScreen = null;
    private static boolean evaluationPending = false;
    private static boolean automatingCurrentScreen = false;

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
                evaluatedScreen = screen;
                evaluationPending = true;
                automatingCurrentScreen = false;
                ClientSessionState.mayTrain();
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
                evaluatedScreen = fresh;
                evaluationPending = false;
                automatingCurrentScreen = true;
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
            case "READY" -> clickCenter(screen);
            case "FINISHED" -> {
                int levelsCleared = (int) Reflect.get(screen, "levelsCleared");
                if (levelsCleared < config.getLevelsToComplete()) {
                    repeating = false;
                }
                clickCenter(screen);
            }
            case "PLAYING" -> {
                ClientSessionState.notifyTrainingStarted();
                if (ClientSessionState.isSessionExpired()) {
                    sessionExpiredThisRun = true;
                }
                if (config.isRepeatTrainingEnabled()) {
                    int levelsCleared = (int) Reflect.get(screen, "levelsCleared");
                    if (levelsCleared >= config.getLevelsToComplete()) {
                        boolean shouldLoop = !ClientSessionState.isSessionExpired();
                        repeatingScreenClass = screen.getClass();
                        repeating = shouldLoop;

                        if (shouldLoop) {
                            pendingRestart = true;
                            pendingRestartAttemptDeadline = System.currentTimeMillis() + PENDING_RESTART_RESPONSE_TIMEOUT_MS;
                            pendingRestartRetryAt = 0L;
                            pendingRestartCountdownLogged = -1;
                        } else {
                            clearPendingRestart();
                        }

                        ClientSessionState.requestFreshStatus();
                        Reflect.invoke(screen, "endGame");
                        return;
                    }
                }
                dispatch(screen);
            }
        }
    }

    private static void dispatch(BaseMinigameScreen screen) {
        if (screen instanceof RythmGameScreen r) {
            RhythmAutomation.tick(r);
        } else if (screen instanceof GravityGameScreen g) {
            GravityAutomation.tick(g);
        } else if (screen instanceof PrecisionGameScreen p) {
            PrecisionAutomation.tick(p);
        } else if (screen instanceof MemoryGameScreen m) {
            MemoryAutomation.tick(m);
        } else if (screen instanceof ControlGameScreen c) {
            ControlAutomation.tick(c);
        }
    }

    private static void clickCenter(BaseMinigameScreen screen) {
        screen.mouseClicked(screen.width / 2.0, screen.height / 2.0, 0);
    }

    private static void resetState() {
        wasAutoTrainerEnabled = false;
        repeating = false;
        clearPendingRestart();
        sessionExpiredThisRun = false;
        evaluatedScreen = null;
        evaluationPending = false;
        automatingCurrentScreen = false;
    }
}