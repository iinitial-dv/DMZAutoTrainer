package com.iinitial.dmzautotrainer;

import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(DMZAutoTrainer.MOD_ID)
public class DMZAutoTrainer {
    public static final String MOD_ID = "dmzautotrainer";

    public DMZAutoTrainer() {
        ModLoadingContext.get().registerConfig(
                ModConfig.Type.CLIENT,
                ConfigManager.SPEC
        );

    }
}
