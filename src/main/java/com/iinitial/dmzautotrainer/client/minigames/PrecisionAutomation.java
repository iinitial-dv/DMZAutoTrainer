package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.common.config.TrainingConfig;
import com.dragonminez.common.training.PrecisionLogic;

import java.util.ArrayList;
import java.util.List;

public class PrecisionAutomation {
    public static void tick(PrecisionLogic precision, InputSink sink) {
        if (precision.isFinished()) return;

        TrainingConfig.PrecisionConfig cfg = precision.cfg();
        double speed = precision.ringSpeed();
        if (speed <= 0) return;

        List<PrecisionLogic.Circle> circles = new ArrayList<>(precision.targets());
        for (PrecisionLogic.Circle circle : circles) {
            if (circle.fading) continue;

            double remaining = circle.ringRadius - cfg.getTargetRadius();
            if (remaining > speed) continue;

            float frac = (float) Math.max(0.0, Math.min(0.999, remaining / speed));
            sink.click(circle.x, circle.y, frac);
        }
    }
}