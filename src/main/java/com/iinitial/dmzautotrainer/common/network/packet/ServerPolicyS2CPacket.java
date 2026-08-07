package com.iinitial.dmzautotrainer.common.network.packet;

import com.iinitial.dmzautotrainer.client.session.ClientSessionState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Server-wide policy, pushed once per connection at login.
 *
 * <p>Separate from {@link SessionStatusS2CPacket} because that packet is per-player,
 * request/reply, and changes constantly, whereas this is fixed for the server's lifetime and
 * must have arrived before the player can open any screen that reads it.</p>
 */
public record ServerPolicyS2CPacket(boolean autoTrainerEnabled) {

    public static void encode(ServerPolicyS2CPacket message, FriendlyByteBuf buffer) {
        buffer.writeBoolean(message.autoTrainerEnabled());
    }

    public static ServerPolicyS2CPacket decode(FriendlyByteBuf buffer) {
        return new ServerPolicyS2CPacket(buffer.readBoolean());
    }

    public static void handle(ServerPolicyS2CPacket message, Supplier<NetworkEvent.Context> contextSupplier) {
        DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientSessionState.updatePolicy(message.autoTrainerEnabled())
        );
        contextSupplier.get().setPacketHandled(true);
    }
}
