package com.iinitial.dmzautotrainer.common.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ConfigManager {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    // values
    public static final ForgeConfigSpec.BooleanValue ENABLE_AUTO_TRAIN;
    public static final ForgeConfigSpec.BooleanValue ENABLE_REPEAT_TRAINING;
    public static final ForgeConfigSpec.IntValue LEVELS_TO_COMPLETE;

    static {
        BUILDER.push("Settings");

        ENABLE_AUTO_TRAIN      = BUILDER
                .comment("Enables the automatic training")
                .define("enableAutoTrainer", false);
        ENABLE_REPEAT_TRAINING = BUILDER
                .comment("Enables training looping/auto completion")
                .define("enableRepeatTraining", true);
        LEVELS_TO_COMPLETE = BUILDER
                .comment("Number of training levels before automatically completing")
                .defineInRange("levelsToComplete", 50, 1, 100);

        BUILDER.pop();
    }

    public static final ForgeConfigSpec SPEC = BUILDER.build();
}
