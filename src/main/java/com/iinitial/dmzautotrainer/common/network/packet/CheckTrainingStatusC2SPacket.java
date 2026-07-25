package com.iinitial.dmzautotrainer.common.network.packet;

import com.iinitial.dmzautotrainer.common.network.NetworkHandler;
import com.iinitial.dmzautotrainer.server.session.SessionStatus;
import com.iinitial.dmzautotrainer.server.session.TrainingSessionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public final class CheckTrainingStatusC2SPacket {
    public static void encode(CheckTrainingStatusC2SPacket message, FriendlyByteBuf buffer) {
    }

    public static CheckTrainingStatusC2SPacket decode(FriendlyByteBuf buffer) {
        return new CheckTrainingStatusC2SPacket();
    }

    public static void handle(CheckTrainingStatusC2SPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        if (player != null) {
            SessionStatus status = TrainingSessionManager.checkStatus(player.server, player.getUUID());
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SessionStatusS2CPacket(status)
            );
        }

        context.setPacketHandled(true);
    }
}