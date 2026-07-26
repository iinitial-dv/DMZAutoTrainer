package com.iinitial.dmzautotrainer.common.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final static Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static ClientConfig clientConfig = new  ClientConfig();
    private static ServerConfig serverConfig =  new  ServerConfig();

    public static ClientConfig client() {
        return clientConfig;
    }

    public static ServerConfig server() {
        return serverConfig;
    }

    public static void loadClientConfig() {
        Path file = FMLPaths.CONFIGDIR.get().resolve("dmzautotrainer-client.json");
        clientConfig = load(file, ClientConfig.class, new ClientConfig());
        save(file, clientConfig);
    }

    public static void loadServerConfig() {
        Path file = FMLPaths.CONFIGDIR.get().resolve("dmzautotrainer-server.json");
        serverConfig = load(file, ServerConfig.class, new ServerConfig());
        save(file, serverConfig);
    }

    public static void saveClientConfig() {
        save(FMLPaths.CONFIGDIR.get().resolve("dmzautotrainer-client.json"), clientConfig);
    }

    public static <T> T load(Path file, Class<T> type, T defaultValues) {
        try {
            if (Files.notExists(file)) return defaultValues;

            try (Reader reader = Files.newBufferedReader(file)) {
                T loaded = GSON.fromJson(reader, type);
                return loaded !=  null ? loaded : defaultValues;
            }
        } catch (IOException | JsonSyntaxException e) {
            LOGGER.error("Failed to load config from {}, falling back to defaults", file, e);
            return defaultValues;
        }
    }

    private static void save(Path file, Object config) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", file, e);
        }
    }
}