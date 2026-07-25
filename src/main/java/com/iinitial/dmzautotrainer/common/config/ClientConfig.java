package com.iinitial.dmzautotrainer.common.config;

public final class ClientConfig {
    public boolean enableAutoTrain = false;
    public boolean enableRepeatTraining = true;
    public float levelsToComplete = 50;

    public boolean isAutoTrainEnabled() { return enableAutoTrain; }

    public boolean isRepeatTrainingEnabled() { return enableRepeatTraining; }

    public float getLevelsToComplete() { return levelsToComplete; }

    public void setAutoTrain(boolean autoTrain) {
        enableAutoTrain = autoTrain;
        ConfigManager.saveClientConfig();
    }

    public void setRepeatTraining(boolean repeatTraining) {
        enableRepeatTraining = repeatTraining;
        ConfigManager.saveClientConfig();
    }

    public void setLevelsToComplete(float levels) {
        levelsToComplete = levels;
        ConfigManager.saveClientConfig();
    }
}
