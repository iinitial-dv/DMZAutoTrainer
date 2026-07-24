package com.iinitial.dmzautotrainer.common.config;

import java.io.ObjectInputFilter;

public final class ClientConfig {
    public static boolean enableAutoTrain = false;
    public static boolean enableRepeatTraining = true;
    public static float   levelsToComplete = 50;

    public static boolean getAutoTrain() { return enableAutoTrain; }

    public static boolean getRepeatTraining() { return enableRepeatTraining; }

    public static float getLevelsToComplete() { return levelsToComplete; }


    public static void setAutoTrain(boolean autoTrain) {
        enableAutoTrain = autoTrain;
        ConfigManager.saveClientConfig();
    }

    public static void setRepeatTraining(boolean repeatTraining) {
        enableRepeatTraining = repeatTraining;
    }

    public static void setLevelsToComplete(float levels) {
        levelsToComplete = levels;
    }
}