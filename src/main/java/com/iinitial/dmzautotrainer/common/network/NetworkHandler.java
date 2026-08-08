package com.iinitial.dmzautotrainer.common.network;

import com.iinitial.dmzautotrainer.DMZAutoTrainer;
import com.iinitial.dmzautotrainer.common.network.packet.ServerPolicyS2CPacket;
import com.iinitial.dmzautotrainer.common.network.packet.SessionStatusS2CPacket;
import com.iinitial.dmzautotrainer.common.network.packet.TrainingSessionActionC2SPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = DMZAutoTrainer.MOD_VERSION;
    private static int packetId;

    /**
     * Registering this channel is what makes the mod mandatory on BOTH sides, so do not
     * "simplify" the two accept predicates away.
     *
     * <p>When a peer has no such channel, Forge does not skip the check: it passes the
     * predicate the sentinel {@code NetworkRegistry.ABSENT}. {@code PROTOCOL_VERSION::equals}
     * returns false for it, the channel lands in {@code missingButRequired}, and
     * {@code HandshakeHandler} disconnects. Both directions are enforced. Opting out would
     * take an explicit {@code NetworkRegistry.acceptMissingOr(...)}, which we never call.</p>
     *
     * <p>{@code PROTOCOL_VERSION} is deliberately the mod version rather than a hand-managed
     * number, so it cannot go stale across a wire-format change. See the project spec for
     * #494 and dmz-plus incident #285, where a hardcoded string held across seven format
     * changes let a mismatched client through and corrupted a player's world data silently.</p>
     */
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

        CHANNEL.messageBuilder(ServerPolicyS2CPacket.class, packetId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ServerPolicyS2CPacket::encode)
                .decoder(ServerPolicyS2CPacket::decode)
                .consumerMainThread(ServerPolicyS2CPacket::handle)
                .add();
    }
}
