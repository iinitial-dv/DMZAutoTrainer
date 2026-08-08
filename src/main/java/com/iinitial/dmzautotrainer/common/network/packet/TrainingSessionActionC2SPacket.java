package com.iinitial.dmzautotrainer.common.network.packet;

import com.iinitial.dmzautotrainer.common.config.ConfigManager;
import com.iinitial.dmzautotrainer.common.network.NetworkHandler;
import com.iinitial.dmzautotrainer.server.session.SessionStatus;
import com.iinitial.dmzautotrainer.server.session.TrainingSessionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public record TrainingSessionActionC2SPacket(Action action) {

    public enum Action {
        REQUEST,
        START,
        END,
        CHECK
    }

    public static void encode(TrainingSessionActionC2SPacket message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.action());
    }

    public static TrainingSessionActionC2SPacket decode(FriendlyByteBuf buffer) {
        return new TrainingSessionActionC2SPacket(buffer.readEnum(Action.class));
    }

    public static void handle(TrainingSessionActionC2SPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        if (player != null) {
            if (!ConfigManager.server().isAutoTrainerEnabled()) {
                NetworkHandler.CHANNEL.send(
                        PacketDistributor.PLAYER.with(() -> player),
                        new SessionStatusS2CPacket(new SessionStatus(false, 0L, 0L, false))
                );
                context.setPacketHandled(true);
                return;
            }

            SessionStatus status = switch (message.action()) {
                case REQUEST -> TrainingSessionManager.requestSession(player.server, player.getUUID());
                case START -> TrainingSessionManager.startSession(player.server, player.getUUID());
                case END -> TrainingSessionManager.endSessionEarly(player.server, player.getUUID());
                case CHECK -> TrainingSessionManager.checkStatus(player.server, player.getUUID());
            };
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SessionStatusS2CPacket(status)
            );
        }

        context.setPacketHandled(true);
    }
}
