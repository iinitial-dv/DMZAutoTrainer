package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.client.gui.character.minigames.MemoryGameScreen;
import com.iinitial.dmzautotrainer.client.Reflect;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class MemoryAutomation {

    public static void tick(MemoryGameScreen screen) {
        String phase = ((Enum<?>) Reflect.get(screen, "memPhase")).name();
        if (!phase.equals("INPUT")) return;

        List<?> pattern = (List<?>) Reflect.get(screen, "pattern");
        int patternIndex = (int) Reflect.get(screen, "patternIndex");
        if (patternIndex >= pattern.size()) return;

        String dir = ((Enum<?>) pattern.get(patternIndex)).name();
        int keyCode = arrowKeyFor(dir);

        screen.keyPressed(keyCode, 0, 0);
        screen.keyReleased(keyCode, 0, 0);
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