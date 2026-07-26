package com.iinitial.dmzautotrainer.server.command;

import com.iinitial.dmzautotrainer.server.debug.SessionDebugTracker;
import com.iinitial.dmzautotrainer.server.session.SessionStatus;
import com.iinitial.dmzautotrainer.server.session.TrainingSessionManager;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.UUID;

public final class DmzTrainerCommand {
    private static final SimpleCommandExceptionType ERR_PLAYER_ONLY =
            new SimpleCommandExceptionType(Component.literal("Only a player can run this."));
    private static final SimpleCommandExceptionType ERR_SINGLE_TARGET =
            new SimpleCommandExceptionType(Component.literal("Specify exactly one player for this."));

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
                                                )))))
                        .then(Commands.literal("get")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .executes(context -> getTargets(
                                                context.getSource(),
                                                GameProfileArgument.getGameProfiles(context, "targets")
                                        )))))
                .then(Commands.literal("debug")
                        .executes(context -> toggleDebug(context.getSource(), null))
                        .then(Commands.argument("target", GameProfileArgument.gameProfile())
                                .executes(context -> toggleDebug(
                                        context.getSource(),
                                        GameProfileArgument.getGameProfiles(context, "target")
                                )))));
    }

    private static int resetTargets(CommandSourceStack source, Collection<GameProfile> targets) {
        int changed = 0;
        for (GameProfile profile : targets) {
            if (TrainingSessionManager.resetCooldown(source.getServer(), profile.getId())) {
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

    private static int getTargets(CommandSourceStack source, Collection<GameProfile> targets) {
        for (GameProfile profile : targets) {
            SessionStatus status = TrainingSessionManager.checkStatus(source.getServer(), profile.getId());
            source.sendSuccess(() -> Component.literal(profile.getName()
                    + ": allowed=" + status.allowed()
                    + " sessionSecondsRemaining=" + status.sessionSecondsRemaining()
                    + " cooldownSecondsRemaining=" + status.cooldownSecondsRemaining()), false);
        }
        return targets.size();
    }

    private static int toggleDebug(CommandSourceStack source, Collection<GameProfile> explicitTargets) throws CommandSyntaxException {
        if (!(source.getEntity() instanceof ServerPlayer watcher)) {
            throw ERR_PLAYER_ONLY.create();
        }

        UUID targetId;
        String targetLabel;
        if (explicitTargets == null) {
            targetId = watcher.getUUID();
            targetLabel = watcher.getGameProfile().getName();
        } else {
            if (explicitTargets.size() != 1) {
                throw ERR_SINGLE_TARGET.create();
            }
            GameProfile profile = explicitTargets.iterator().next();
            targetId = profile.getId();
            targetLabel = profile.getName();
        }

        boolean nowOn = SessionDebugTracker.toggle(watcher, targetId);
        source.sendSuccess(() -> Component.literal(nowOn
                ? "Debug streaming ON for " + targetLabel + " (updates ~every second)."
                : "Debug streaming OFF for " + targetLabel + "."), false);
        return 1;
    }
}