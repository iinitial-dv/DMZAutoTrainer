package com.iinitial.dmzautotrainer.common.network.packet;

import com.iinitial.dmzautotrainer.common.network.NetworkHandler;
import com.iinitial.dmzautotrainer.server.session.SessionStatus;
import com.iinitial.dmzautotrainer.server.session.TrainingSessionManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public final class EndTrainingSessionC2SPacket {
    public static void encode(EndTrainingSessionC2SPacket message, FriendlyByteBuf buffer) {
    }

    public static EndTrainingSessionC2SPacket decode(FriendlyByteBuf buffer) {
        return new EndTrainingSessionC2SPacket();
    }

    public static void handle(EndTrainingSessionC2SPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        ServerPlayer player = context.getSender();

        if (player != null) {
            SessionStatus status = TrainingSessionManager.endSessionEarly(player.server, player.getUUID());
            NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SessionStatusS2CPacket(status)
            );
        }

        context.setPacketHandled(true);
    }
}
