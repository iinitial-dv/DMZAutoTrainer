package com.iinitial.dmzautotrainer.server;

import com.iinitial.dmzautotrainer.DMZAutoTrainer;
import com.iinitial.dmzautotrainer.server.command.CooldownCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DMZAutoTrainer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        CooldownCommand.register(event.getDispatcher());
    }
}
