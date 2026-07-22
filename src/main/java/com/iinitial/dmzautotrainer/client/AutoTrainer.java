package com.iinitial.dmzautotrainer.client;

import com.dragonminez.client.gui.character.minigames.*;
import com.iinitial.dmzautotrainer.client.minigames.*;
import com.iinitial.dmzautotrainer.common.config.ClientConfig;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class AutoTrainer {

    private static boolean repeating = false;
    private static Class<? extends BaseMinigameScreen> repeatingScreenClass = null;

    public static void globalTick(Minecraft mc) {
        if (!ClientConfig.getAutoTrain()) {
            repeating = false;
            return;
        }

        if (mc.screen instanceof BaseMinigameScreen screen) {
            tick(screen);
        } else if (repeating) {
            if (escapeHeld(mc)) {
                repeating = false; // real player input — stop the loop
                return;
            }
            restartLoop(mc);
        }
    }

    private static void tick(BaseMinigameScreen screen) {
        String stage = ((Enum<?>) Reflect.get(screen, "stage")).name();

        switch (stage) {
            case "READY", "FINISHED" -> clickCenter(screen);
            case "PLAYING" -> {
                if (ClientConfig.getRepeatTraining()) {
                    int levelsCleared = (int) Reflect.get(screen, "levelsCleared");
                    if (levelsCleared >= ClientConfig.getLevelsToComplete()) {
                        repeatingScreenClass = screen.getClass();
                        repeating = true;
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

    private static void restartLoop(Minecraft mc) {
        try {
            BaseMinigameScreen fresh = repeatingScreenClass.getDeclaredConstructor().newInstance();
            mc.setScreen(fresh);
        } catch (Exception e) {
            repeating = false;
            throw new RuntimeException("Failed to restart minigame for repeat training", e);
        }
    }

    private static boolean escapeHeld(Minecraft mc) {
        long window = mc.getWindow().getWindow();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;
    }
}