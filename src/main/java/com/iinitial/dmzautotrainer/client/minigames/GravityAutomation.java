package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.client.gui.character.minigames.GravityGameScreen;
import com.iinitial.dmzautotrainer.client.Reflect;
import org.lwjgl.glfw.GLFW;

public class GravityAutomation {
    public static void tick(GravityGameScreen screen) {
        int neededSide = (int) Reflect.get(screen, "neededSide");
        int keyCode = neededSide == -1 ? GLFW.GLFW_KEY_LEFT : GLFW.GLFW_KEY_RIGHT;

        screen.keyPressed(keyCode, 0, 0);
        screen.keyReleased(keyCode, 0, 0);
    }
}