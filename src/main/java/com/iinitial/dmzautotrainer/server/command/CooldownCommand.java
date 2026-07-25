package com.iinitial.dmzautotrainer.server.command;

import com.iinitial.dmzautotrainer.server.session.TrainingSessionManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class CooldownCommand {
    private CooldownCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dmztrainer")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("cooldown")
                        .then(Commands.literal("reset")
                                .then(Commands.literal("everyone")
                                        .executes(context -> resetEveryone(context.getSource())))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> resetPlayer(
                                                context.getSource(),
                                                EntityArgument.getPlayer(context, "player")
                                        ))))
                        .then(Commands.literal("set")
                                .then(Commands.literal("everyone")
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                                .executes(context -> setEveryone(
                                                        context.getSource(),
                                                        IntegerArgumentType.getInteger(context, "seconds")
                                                ))))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                                .executes(context -> setPlayer(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "player"),
                                                        IntegerArgumentType.getInteger(context, "seconds")
                                                )))))));
    }

    private static int resetEveryone(CommandSourceStack source) {
        int changed = TrainingSessionManager.resetCooldowns(source.getServer());
        source.sendSuccess(() -> Component.literal("Reset cooldowns for " + changed + " player(s)."), true);
        return changed;
    }

    private static int resetPlayer(CommandSourceStack source, ServerPlayer player) {
        boolean changed = TrainingSessionManager.resetCooldown(source.getServer(), player.getUUID());
        source.sendSuccess(
                () -> Component.literal(changed
                        ? "Reset " + player.getGameProfile().getName() + "'s cooldown."
                        : player.getGameProfile().getName() + " has no cooldown to reset."),
                true
        );
        return changed ? 1 : 0;
    }

    private static int setEveryone(CommandSourceStack source, int seconds) {
        int changed = TrainingSessionManager.setCooldowns(source.getServer(), seconds);
        source.sendSuccess(
                () -> Component.literal("Set cooldown to " + seconds + " second(s) for " + changed + " player(s)."),
                true
        );
        return changed;
    }

    private static int setPlayer(CommandSourceStack source, ServerPlayer player, int seconds) {
        TrainingSessionManager.setCooldown(source.getServer(), player.getUUID(), seconds);
        source.sendSuccess(
                () -> Component.literal("Set " + player.getGameProfile().getName() + "'s cooldown to " + seconds + " second(s)."),
                true
        );
        return 1;
    }
}
