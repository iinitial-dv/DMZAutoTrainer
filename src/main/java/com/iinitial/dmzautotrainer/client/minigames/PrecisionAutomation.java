package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.client.gui.character.minigames.PrecisionGameScreen;
import com.dragonminez.common.config.ConfigManager;
import com.iinitial.dmzautotrainer.client.Reflect;

import java.util.ArrayList;
import java.util.List;

public class PrecisionAutomation {
    public static void tick(PrecisionGameScreen screen) {
        List<?> liveTargets = (List<?>) Reflect.get(screen, "targets");
        List<?> targets = new ArrayList<>(liveTargets);

        var cfg = ConfigManager.getTrainingConfig().getPrecision();
        int targetRadius = cfg.getTargetRadius();
        int perfectWindow = cfg.getPerfectWindow();

        for (Object circle : targets) {
            boolean fading = (boolean) Reflect.get(circle, "fading");
            if (fading) continue;

            float ringRadius = (float) Reflect.get(circle, "ringRadius");
            float distance = Math.abs(ringRadius - targetRadius);

            if (distance <= perfectWindow) {
                int x = (int) Reflect.get(circle, "x");
                int y = (int) Reflect.get(circle, "y");
                screen.mouseClicked(x, y, 0);
            }
        }
    }
}