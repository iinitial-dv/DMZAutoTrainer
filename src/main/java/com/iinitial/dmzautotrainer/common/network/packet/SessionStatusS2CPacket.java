package com.iinitial.dmzautotrainer.common.network.packet;

import com.iinitial.dmzautotrainer.client.session.ClientSessionState;
import com.iinitial.dmzautotrainer.server.session.SessionStatus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record SessionStatusS2CPacket(
        boolean allowed,
        long sessionSecondsRemaining,
        long cooldownSecondsRemaining,
        boolean sessionsEnabled
) {
    public SessionStatusS2CPacket(SessionStatus status) {
        this(status.allowed(), status.sessionSecondsRemaining(), status.cooldownSecondsRemaining(), status.sessionsEnabled());
    }

    public static void encode(SessionStatusS2CPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.allowed());
        buffer.writeLong(message.sessionSecondsRemaining());
        buffer.writeLong(message.cooldownSecondsRemaining());
        buffer.writeBoolean(message.sessionsEnabled());
    }

    public static SessionStatusS2CPacket decode(FriendlyByteBuf buffer) {
        return new SessionStatusS2CPacket(
                buffer.readBoolean(),
                buffer.readLong(),
                buffer.readLong(),
                buffer.readBoolean()
        );
    }

    public static void handle(SessionStatusS2CPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientSessionState.update(message)
        );
        contextSupplier.get().setPacketHandled(true);
    }
}