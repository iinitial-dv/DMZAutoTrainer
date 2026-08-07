package com.iinitial.dmzautotrainer.client.autotrainer;

import com.dragonminez.client.gui.character.minigames.*;
import com.iinitial.dmzautotrainer.client.minigames.*;
import com.iinitial.dmzautotrainer.client.session.ClientSessionState;
import com.iinitial.dmzautotrainer.common.config.ClientConfig;
import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import net.minecraft.client.Minecraft;

public class AutoTrainer {
    private static boolean repeating = false;
    private static boolean wasAutoTrainerEnabled = false;
    private static boolean pendingRestart = false;
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
                pendingRestart = false;
                repeating = false;
                ClientSessionState.endSessionEarly();
            } else if (pendingRestart) {
                pendingRestart = false;
                repeating = false;
                if (ClientSessionState.mayTrain()) {
                    try {
                        BaseMinigameScreen fresh = repeatingScreenClass.getDeclaredConstructor().newInstance();
                        mc.setScreen(fresh);
                        repeating = true;
                        evaluatedScreen = fresh;
                        evaluationPending = false;
                        automatingCurrentScreen = true;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to restart minigame for repeat training", e);
                    }
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
                        pendingRestart = shouldLoop;
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
        sessionExpiredThisRun = false;
        evaluatedScreen = null;
        evaluationPending = false;
        automatingCurrentScreen = false;
    }
}