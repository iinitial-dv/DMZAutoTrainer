package com.iinitial.dmzautotrainer.common.network;

import com.iinitial.dmzautotrainer.DMZAutoTrainer;
import com.iinitial.dmzautotrainer.common.network.packet.SessionStatusS2CPacket;
import com.iinitial.dmzautotrainer.common.network.packet.TrainingSessionActionC2SPacket;
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
        CHANNEL.messageBuilder(TrainingSessionActionC2SPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TrainingSessionActionC2SPacket::encode)
                .decoder(TrainingSessionActionC2SPacket::decode)
                .consumerMainThread(TrainingSessionActionC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(SessionStatusS2CPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SessionStatusS2CPacket::encode)
                .decoder(SessionStatusS2CPacket::decode)
                .consumerMainThread(SessionStatusS2CPacket::handle)
                .add();
    }
}
