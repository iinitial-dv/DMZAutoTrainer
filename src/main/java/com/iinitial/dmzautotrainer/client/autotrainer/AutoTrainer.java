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
    // --- DEBUG LOGGING
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String TAG = "[DMZAT-DEBUG]";

    private static final long PENDING_RESTART_TIMEOUT_MS = 5000L;
    private static boolean repeating = false;
    private static boolean wasAutoTrainerEnabled = false;
    private static boolean pendingRestart = false;
    private static long pendingRestartDeadline = 0L;
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
                // DEBUG LOGGING
                LOGGER.info("{} globalTick(): enableAutoTrain toggled off mid-run. Calling endSessionEarly().", TAG);
                ClientSessionState.endSessionEarly();
            }
            resetState();
            return;
        }
        wasAutoTrainerEnabled = true;

        if (mc.screen instanceof BaseMinigameScreen screen) {
            if (screen != evaluatedScreen) {
                // DEBUG LOGGING
                LOGGER.info("{} globalTick(): NEW screen detected ({}). Requesting mayTrain() evaluation.",
                        TAG, screen.getClass().getSimpleName());
                evaluatedScreen = screen;
                evaluationPending = true;
                automatingCurrentScreen = false;
                ClientSessionState.mayTrain();
            }

            if (evaluationPending) {
                if (ClientSessionState.isAwaitingResponse()) {
                    // DEBUG LOGGING
                    if (System.currentTimeMillis() % 1000 < 50) {
                        LOGGER.info("{} globalTick(): evaluationPending=true, still awaitingServerResponse. Blocked.", TAG);
                    }
                    return;
                }
                evaluationPending = false;
                automatingCurrentScreen = ClientSessionState.isAllowed();
                // DEBUG LOGGING
                LOGGER.info("{} globalTick(): evaluation resolved. automatingCurrentScreen={}", TAG, automatingCurrentScreen);
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
                // DEBUG LOGGING
                LOGGER.info("{} globalTick(): sessionExpiredThisRun=true, off the minigame screen. Ending session, repeating=false.", TAG);
                sessionExpiredThisRun = false;
                pendingRestart = false;
                repeating = false;
                ClientSessionState.endSessionEarly();
            } else if (pendingRestart) {
                if (ClientSessionState.isAwaitingResponse()) {
                    if (System.currentTimeMillis() < pendingRestartDeadline) {
                        // DEBUG LOGGING
                        LOGGER.info("{} globalTick(): pendingRestart=true, off screen. Still awaitingServerResponse, within timeout window. Waiting.", TAG);
                        return;
                    }
                    // DEBUG LOGGING
                    LOGGER.warn("{} globalTick(): pendingRestart=true, off screen. TIMED OUT waiting for CHECK response " + "(> {}ms). Giving up on this restart - repeating and pendingRestart set false.", TAG, PENDING_RESTART_TIMEOUT_MS);
                    pendingRestart = false;
                    repeating = false;
                    return;
                }

                pendingRestart = false;
                repeating = false;

                boolean canTrain = ClientSessionState.isAllowed();
                // DEBUG LOGGING
                LOGGER.info("{} globalTick(): pendingRestart=true, off screen. Response resolved, isAllowed()={}. repeatingScreenClass={}", TAG, canTrain, repeatingScreenClass != null ? repeatingScreenClass.getSimpleName() : "null");

                if (canTrain) {
                    try {
                        BaseMinigameScreen fresh = repeatingScreenClass.getDeclaredConstructor().newInstance();
                        mc.setScreen(fresh);
                        repeating = true;
                        evaluatedScreen = fresh;
                        evaluationPending = false;
                        automatingCurrentScreen = true;
                        // DEBUG LOGGING
                        LOGGER.info("{} globalTick(): restart SUCCESS, new {} instantiated and set.", TAG, fresh.getClass().getSimpleName());
                    } catch (Exception e) {
                        // DEBUG LOGGING
                        LOGGER.error("{} globalTick(): restart FAILED to instantiate/set new screen. repeating stays false.", TAG, e);
                        throw new RuntimeException("Failed to restart minigame for repeat training", e);
                    }
                }
                // DEBUG LOGGING
                else {
                    LOGGER.info("{} globalTick(): server denied restart (isAllowed()=false after response). Stopping repeat loop.", TAG);
                }
            }
        }
    }

    private static void tick(BaseMinigameScreen screen, ClientConfig config) {
        String stage = ((Enum<?>) Reflect.get(screen, "stage")).name();

        switch (stage) {
            case "READY" -> clickCenter(screen);
            case "FINISHED" -> {
                int levelsCleared = (int) Reflect.get(screen, "levelsCleared");
                // DEBUG LOGGING
                LOGGER.info("{} tick(): stage=FINISHED. levelsCleared={}, levelsToComplete={}, repeating(before)={}", TAG, levelsCleared, config.getLevelsToComplete(), repeating);
                if (levelsCleared < config.getLevelsToComplete()) {
                    // DEBUG LOGGING
                    LOGGER.info("{} tick(): levelsCleared < levelsToComplete -> forcing repeating=false.", TAG);
                    repeating = false;
                }
                clickCenter(screen);
            }
            case "PLAYING" -> {
                ClientSessionState.notifyTrainingStarted();
                if (ClientSessionState.isSessionExpired()) {
                    // DEBUG LOGGING
                    LOGGER.info("{} tick(): session expired mid-PLAYING. Flagging sessionExpiredThisRun=true.", TAG);
                    sessionExpiredThisRun = true;
                }
                if (config.isRepeatTrainingEnabled()) {
                    int levelsCleared = (int) Reflect.get(screen, "levelsCleared");
                    if (levelsCleared >= config.getLevelsToComplete()) {
                        boolean shouldLoop = !ClientSessionState.isSessionExpired();
                        // DEBUG LOGGING
                        LOGGER.info("{} tick(): quota reached ({} >= {}). shouldLoop={}. Ending game, requesting fresh status.", TAG, levelsCleared, config.getLevelsToComplete(), shouldLoop);
                        repeatingScreenClass = screen.getClass();
                        repeating = shouldLoop;
                        pendingRestart = shouldLoop;
                        pendingRestartDeadline = System.currentTimeMillis() + PENDING_RESTART_TIMEOUT_MS;
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
        pendingRestart = false;
        pendingRestartDeadline = 0L;
        sessionExpiredThisRun = false;
        evaluatedScreen = null;
        evaluationPending = false;
        automatingCurrentScreen = false;
    }
}