package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.client.gui.character.minigames.RythmGameScreen;
import com.dragonminez.common.config.ConfigManager;
import com.iinitial.dmzautotrainer.client.Reflect;
import com.iinitial.dmzautotrainer.client.SpoofedInput;

import org.lwjgl.glfw.GLFW;
import java.util.ArrayList;
import java.util.List;

public class RhythmAutomation {
    public static void tick(RythmGameScreen screen) {
        List<?> liveArrows = (List<?>) Reflect.get(screen, "arrows");
        List<?> arrows = new ArrayList<>(liveArrows);

        int laneLeftX = (int) Reflect.get(screen, "laneLeftX");
        int laneRightX = (int) Reflect.get(screen, "laneRightX");

        var cfg = ConfigManager.getTrainingConfig().getRhythm();
        int perfectWindow = cfg.getPerfectWindow();

        for (Object note : arrows) {
            boolean leftLane = (boolean) Reflect.get(note, "leftLane");
            boolean activated = (boolean) Reflect.get(note, "activated");
            boolean isHold = (boolean) Reflect.get(note, "isHold");
            float x = (float) Reflect.get(note, "x");
            String dir = ((Enum<?>) Reflect.get(note, "direction")).name();
            int keyCode = arrowKeyFor(dir);
            int targetX = leftLane ? laneLeftX : laneRightX;

            if (!activated) {
                if (Math.abs(x - targetX) <= perfectWindow) {
                    screen.keyPressed(keyCode, 0, 0);
                    screen.keyReleased(keyCode, 0, 0);

                    if (isHold) {
                        SpoofedInput.press(keyCode);
                    }
                }
            } else {
                int holdRemaining = (int) Reflect.get(note, "holdRemaining");
                if (holdRemaining > 0) {
                    SpoofedInput.press(keyCode);
                } else {
                    SpoofedInput.release(keyCode);
                }
            }
        }
    }

    private static int arrowKeyFor(String dir) {
        return switch (dir) {
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            default -> throw new IllegalStateException(dir);
        };
    }
}