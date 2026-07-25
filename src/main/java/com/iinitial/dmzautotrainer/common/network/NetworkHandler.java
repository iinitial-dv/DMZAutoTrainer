package com.iinitial.dmzautotrainer.common.network;

import com.iinitial.dmzautotrainer.DMZAutoTrainer;
import com.iinitial.dmzautotrainer.common.network.packet.EndTrainingSessionC2SPacket;
import com.iinitial.dmzautotrainer.common.network.packet.RequestTrainingSessionC2SPacket;
import com.iinitial.dmzautotrainer.common.network.packet.SessionStatusS2CPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = DMZAutoTrainer.MOD_VERSION;
    private static int packetId;

    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(DMZAutoTrainer.MOD_ID, "main"))
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    static {
        CHANNEL.messageBuilder(RequestTrainingSessionC2SPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(RequestTrainingSessionC2SPacket::encode)
                .decoder(RequestTrainingSessionC2SPacket::decode)
                .consumerMainThread(RequestTrainingSessionC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(EndTrainingSessionC2SPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(EndTrainingSessionC2SPacket::encode)
                .decoder(EndTrainingSessionC2SPacket::decode)
                .consumerMainThread(EndTrainingSessionC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(SessionStatusS2CPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SessionStatusS2CPacket::encode)
                .decoder(SessionStatusS2CPacket::decode)
                .consumerMainThread(SessionStatusS2CPacket::handle)
                .add();
    }
}
