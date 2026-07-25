package com.iinitial.dmzautotrainer;

import com.iinitial.dmzautotrainer.client.DMZAutoTrainerClient;
import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.iinitial.dmzautotrainer.common.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(DMZAutoTrainer.MOD_ID)
public class DMZAutoTrainer {
    public static final String MOD_ID = "dmzautotrainer";
    public static final String MOD_VERSION = FMLLoader.getLoadingModList().getModFileById(MOD_ID).versionString();

    public DMZAutoTrainer() {
        NetworkHandler.CHANNEL.getClass();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> DMZAutoTrainerClient::init);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStart);
    }

    private void onServerStart(ServerAboutToStartEvent event) {
        ConfigManager.loadServerConfig();
    }
}
