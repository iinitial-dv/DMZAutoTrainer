package com.iinitial.dmzautotrainer.common.config;

public final class ClientConfig {
    public boolean enableAutoTrain = false;
    public boolean enableRepeatTraining = true;

    public boolean isAutoTrainEnabled() { return enableAutoTrain; }

    public boolean isRepeatTrainingEnabled() { return enableRepeatTraining; }

    public void setAutoTrain(boolean autoTrain) {
        enableAutoTrain = autoTrain;
        ConfigManager.saveClientConfig();
    }

    public void setRepeatTraining(boolean repeatTraining) {
        enableRepeatTraining = repeatTraining;
        ConfigManager.saveClientConfig();
    }
}