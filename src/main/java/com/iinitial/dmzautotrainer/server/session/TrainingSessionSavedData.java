package com.iinitial.dmzautotrainer.server.session;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TrainingSessionSavedData extends SavedData {
    private static final String DATA_NAME = "dmzautotrainer_sessions";
    private final Map<UUID, PlayerSessionTimes> players = new HashMap<>();

    public static TrainingSessionSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                TrainingSessionSavedData::load,
                TrainingSessionSavedData::new,
                DATA_NAME
        );
    }

    private static TrainingSessionSavedData load(CompoundTag tag) {
        TrainingSessionSavedData data = new TrainingSessionSavedData();
        ListTag entries = tag.getList("players", Tag.TAG_COMPOUND);

        for (Tag entry : entries) {
            CompoundTag playerTag = (CompoundTag) entry;
            UUID uuid = playerTag.getUUID("uuid");
            long sessionEndsAt = playerTag.getLong("sessionEndsAt");
            long cooldownEndsAt = playerTag.getLong("cooldownEndsAt");

            data.players.put(uuid, new PlayerSessionTimes(sessionEndsAt, cooldownEndsAt));
        }
        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag entries = new ListTag();

        for (Map.Entry<UUID, PlayerSessionTimes> entry : players.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            playerTag.putUUID("uuid", entry.getKey());
            playerTag.putLong("sessionEndsAt", entry.getValue().sessionEndsAt());
            playerTag.putLong("cooldownEndsAt", entry.getValue().cooldownEndsAt());
            entries.add(playerTag);
        }
        tag.put("players", entries);
        return tag;
    }

    public PlayerSessionTimes get(UUID uuid) {
        return players.get(uuid);
    }

    public void put(UUID uuid, PlayerSessionTimes times) {
        players.put(uuid, times);
        setDirty();
    }

    public void remove(UUID uuid) {
        if (players.remove(uuid) != null) {
            setDirty();
        }
    }
}
