package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.client.gui.character.minigames.ControlGameScreen;
import com.iinitial.dmzautotrainer.client.Reflect;

public class ControlAutomation {

    public static void tick(ControlGameScreen screen) {
        float zoneMid = (float) Reflect.get(screen, "zoneMid");
        Reflect.set(screen, "cursorX", zoneMid);
    }
}