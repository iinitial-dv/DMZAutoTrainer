package com.iinitial.dmzautotrainer;

import com.iinitial.dmzautotrainer.network.NetworkHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(DMZAutoTrainer.MOD_ID)
public class DMZAutoTrainer {
    public static final String MOD_ID = "dmzautotrainer";
    public static final String MOD_VERSION = FMLLoader.getLoadingModList().getModFileById(MOD_ID).versionString();

    public DMZAutoTrainer() {
        NetworkHandler.CHANNEL.getClass();
    }
}
