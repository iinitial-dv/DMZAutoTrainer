package com.iinitial.dmzautotrainer.client;

import com.dragonminez.client.gui.character.minigames.*;
import com.iinitial.dmzautotrainer.mixin.BaseMinigameScreenAccessor;
import net.minecraft.client.Minecraft;

public class AutoTrainer {
    public static Minecraft mc = Minecraft.getInstance();
    public static void tick(BaseMinigameScreen s) {
        if (mc.screen instanceof BaseMinigameScreen screen) {
            BaseMinigameScreenAccessor accessor = (BaseMinigameScreenAccessor) screen;

            var stage = accessor.getStage();

            switch (stage) {
                case READY -> {
                    // Click to start
                }

                case PLAYING -> {
                    // Perform automation
                }

                case FINISHED -> {
                    // Click to continue / collect rewards
                }
            }
        }
    }

    public static void stop() {

    }
}
