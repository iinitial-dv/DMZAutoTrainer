package com.iinitial.dmzautotrainer.common.config;

public final class ClientConfig {
    // getters for config file
    public static boolean getAutoTrain() {
        return ConfigManager.ENABLE_AUTO_TRAIN.get();
    }
    public static boolean getRepeatTraining() {
        return ConfigManager.ENABLE_REPEAT_TRAINING.get();
    }
    public static int     getLevelsToComplete() {
        return ConfigManager.LEVELS_TO_COMPLETE.get();
    }
    // setters for config file
    public static void setAutoTrain(boolean value) {
        ConfigManager.ENABLE_AUTO_TRAIN.set(value);
        ConfigManager.SPEC.save();
    }
    public static void setRepeatTraining(boolean value) {
        ConfigManager.ENABLE_REPEAT_TRAINING.set(value);
        ConfigManager.SPEC.save();
    }
    public static void setLevelsToComplete(float value) {
        ConfigManager.LEVELS_TO_COMPLETE.set((int)value);
        ConfigManager.SPEC.save();
    }
}
