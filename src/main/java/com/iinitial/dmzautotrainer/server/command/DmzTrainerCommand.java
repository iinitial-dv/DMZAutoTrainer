package com.iinitial.dmzautotrainer.server.command;

import com.iinitial.dmzautotrainer.server.session.TrainingSessionManager;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.UUID;

public final class CooldownCommand {
    private CooldownCommand() {
public final class DmzTrainerCommand {
    private DmzTrainerCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dmztrainer")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("cooldown")
                        .then(Commands.literal("reset")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .executes(context -> resetTargets(
                                                context.getSource(),
                                                GameProfileArgument.getGameProfiles(context, "targets")
                                        ))))
                        .then(Commands.literal("set")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .then(Commands.argument("seconds", IntegerArgumentType.integer(0))
                                                .executes(context -> setTargets(
                                                        context.getSource(),
                                                        GameProfileArgument.getGameProfiles(context, "targets"),
                                                        IntegerArgumentType.getInteger(context, "seconds")
                                                )))))));
    }

    private static int resetTargets(CommandSourceStack source, Collection<GameProfile> targets) {
        int changed = 0;
        for (GameProfile profile : targets) {
            UUID uuid = profile.getId();
            if (TrainingSessionManager.resetCooldown(source.getServer(), uuid)) {
                changed++;
            }
        }
        int total = targets.size();
        int finalChanged = changed;
        source.sendSuccess(() -> Component.literal("Reset cooldown for " + finalChanged + "/" + total + " player(s)."), true);
        return changed;
    }

    private static int setTargets(CommandSourceStack source, Collection<GameProfile> targets, int seconds) {
        for (GameProfile profile : targets) {
            TrainingSessionManager.setCooldown(source.getServer(), profile.getId(), seconds);
        }
        int total = targets.size();
        source.sendSuccess(() -> Component.literal("Set cooldown to " + seconds + " second(s) for " + total + " player(s)."), true);
        return total;
    }
}
