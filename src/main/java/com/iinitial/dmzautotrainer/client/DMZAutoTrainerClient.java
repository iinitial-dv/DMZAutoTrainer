package com.iinitial.dmzautotrainer.client;

import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class DMZAutoTrainerClient {
    public static void init() {
        ConfigManager.loadClientConfig();
    }
}
