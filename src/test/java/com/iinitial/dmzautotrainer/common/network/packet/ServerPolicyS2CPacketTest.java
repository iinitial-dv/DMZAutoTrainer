package com.iinitial.dmzautotrainer.common.network.packet;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerPolicyS2CPacketTest {

    @Test
    void roundTripsAnEnabledPolicy() {
        assertEquals(new ServerPolicyS2CPacket(true),
                encodeThenDecode(new ServerPolicyS2CPacket(true)));
    }

    @Test
    void roundTripsADisabledPolicy() {
        assertEquals(new ServerPolicyS2CPacket(false),
                encodeThenDecode(new ServerPolicyS2CPacket(false)),
                "A disabled policy must survive the wire, or the kill switch never reaches the client.");
    }

    private static ServerPolicyS2CPacket encodeThenDecode(ServerPolicyS2CPacket packet) {
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        ServerPolicyS2CPacket.encode(packet, buffer);
        return ServerPolicyS2CPacket.decode(buffer);
    }
}
