package com.iinitial.dmzautotrainer.client.minigames;

import com.dragonminez.common.training.MemoryLogic;
import com.dragonminez.common.training.MinigameEvent;

public class MemoryAutomation {
    public static void tick(MemoryLogic memory, InputSink sink) {
        if (memory.isFinished() || memory.phase() != MemoryLogic.Phase.TYPING) return;

        int[] sequence = memory.sequence();
        int index = memory.index();
        if (index >= sequence.length) return;

        sink.tap(MinigameEvent.CHAR_BASE + sequence[index], 0f);
    }
}