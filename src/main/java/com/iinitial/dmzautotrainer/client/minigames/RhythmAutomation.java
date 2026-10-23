package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.common.training.RhythmLogic;

import java.util.ArrayList;
import java.util.List;

public class RhythmAutomation {
    public static void tick(RhythmLogic rhythm, InputSink sink) {
        if (rhythm.isFinished()) return;

        double now = RhythmLogic.songTimeMs(rhythm.tickCount(), 0f);
        double next = now + RhythmLogic.MS_PER_TICK;
        List<RhythmLogic.Note> notes = new ArrayList<>(rhythm.notes());

        boolean[] holding = new boolean[RhythmLogic.LANES];
        for (RhythmLogic.Note note : notes) {
            if (note.state != RhythmLogic.NoteState.HOLDING) continue;
            double end = note.timeMs + note.lengthMs;
            if (end < next) {
                sink.key(note.lane, false, fraction(end, now));
            } else {
                holding[note.lane] = true;
            }
        }

        for (int lane = 0; lane < RhythmLogic.LANES; lane++) {
            if (rhythm.isInputHeld(lane) && !holding[lane]) sink.key(lane, false, 0f);
        }

        if (rhythm.isResting()) return;

        for (RhythmLogic.Note note : notes) {
            if (note.state != RhythmLogic.NoteState.PENDING || note.timeMs >= next) continue;
            if (rhythm.isInputHeld(note.lane)) continue;

            float frac = fraction(note.timeMs, now);
            sink.key(note.lane, true, frac);
            if (note.lengthMs <= 0) sink.key(note.lane, false, frac);
        }
    }

    private static float fraction(double timeMs, double tickStartMs) {
        double frac = (timeMs - tickStartMs) / RhythmLogic.MS_PER_TICK;
        return (float) Math.max(0.0, Math.min(0.996, frac));
    }
}