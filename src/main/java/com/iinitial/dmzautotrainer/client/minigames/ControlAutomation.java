package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.common.training.KiControlLogic;
import com.dragonminez.common.training.MinigameEvent;

public class ControlAutomation {
    private static final double GAIN = 0.25;

    public static void tick(KiControlLogic control, InputSink sink) {
        if (control.isFinished()) return;

        double center = control.barY() + control.barHeight() / 2.0;
        double wanted = control.orbVelocity() + clamp(GAIN * (control.orbY() - center), -control.cfg().getMaxBarSpeed(), control.cfg().getMaxBarSpeed());
        boolean hold = control.barVelocity() < wanted;

        boolean held = control.isInputHeld(MinigameEvent.ACTION);
        if (hold != held) sink.key(MinigameEvent.ACTION, hold, 0f);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}