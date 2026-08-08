package com.iinitial.dmzautotrainer.common.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServerConfigTest {

    @Test
    void autoTrainerDefaultsToEnabled() {
        assertTrue(new ServerConfig().isAutoTrainerEnabled(),
                "A fresh ServerConfig must enable the auto trainer, so installing the update is a no-op for existing servers.");
    }

    @Test
    void configPredatingTheKeyKeepsTheEnabledDefault(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("dmzautotrainer-server.json");
        Files.writeString(file, "{\"enableSessions\": true, \"sessionDuration\": 600, \"sessionCooldown\": 600}");

        ServerConfig loaded = ConfigManager.load(file, ServerConfig.class, new ServerConfig());

        assertTrue(loaded.isAutoTrainerEnabled(),
                "An existing config written before this key existed must not silently disable the trainer.");
        assertTrue(loaded.isSessionsEnabled(),
                "Keys that were already present must still load.");
    }

    @Test
    void configCanDisableTheAutoTrainer(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("dmzautotrainer-server.json");
        Files.writeString(file, "{\"enableAutoTrainer\": false}");

        ServerConfig loaded = ConfigManager.load(file, ServerConfig.class, new ServerConfig());

        assertFalse(loaded.isAutoTrainerEnabled(),
                "The switch must actually take effect when a server owner sets it.");
    }
}
