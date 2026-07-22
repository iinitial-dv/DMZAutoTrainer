package com.iinitial.dmzautotrainer.client;

import com.dragonminez.client.gui.character.minigames.BaseMinigameScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(
        modid = "dmzautotrainer",
        value = Dist.CLIENT
)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.screen instanceof BaseMinigameScreen screen) {
            System.out.println(screen.getClass().getSimpleName());
        }
    }
}