package com.iinitial.dmzautotrainer.server;

import com.iinitial.dmzautotrainer.DMZAutoTrainer;
import com.iinitial.dmzautotrainer.server.command.CooldownCommand;
import com.iinitial.dmzautotrainer.server.command.DmzTrainerCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DMZAutoTrainer.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerEvents {
    private ServerEvents() {
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        DmzTrainerCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            SessionDebugTracker.tick(server);
        }
    }
}