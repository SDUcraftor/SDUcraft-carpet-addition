package top.sducraft.config.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;

public class ChatAIConfig {
    public static File configFile;
    public static List<APIConfig> configList = new ArrayList<>();
    public static Map<UUID, APIConfig> playerActiveConfig = new HashMap<>();

    public static class APIConfig {
        public String provider = "openai";
        public String apiKey = "your-api-key-or-token";
        public String apiUrl = "https://api.openai.com/v1/chat/completions";
        public String authorizationHeader = "Authorization";
        public String model = "gpt-3.5-turbo";
        public double temperature = 0.7;
        public int maxTokens = 2048;
        public String systemPrompt = "你是一名乐于助人的 Minecraft 助手，回答简洁、准确。";
    }

    public static void init(MinecraftServer server) {
        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        File configDir = new File(worldDir, "config/carpet-sducraft-addition");
        if (!configDir.exists()) configDir.mkdirs();

        configFile = new File(configDir, "openai.json");
        loadConfig();
    }

    private static void loadConfig() {
        try {
            if (configFile.exists()) {
                FileReader reader = new FileReader(configFile);
                Type listType = new TypeToken<List<APIConfig>>() {
                }.getType();
                configList = new Gson().fromJson(reader, listType);
                reader.close();
            } else {
                configList.add(new APIConfig());
                saveConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(configList, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static APIConfig getActiveConfig(UUID uuid) {
        for (UUID uuid1 : playerActiveConfig.keySet()) {
            if (uuid.equals(uuid1)) return playerActiveConfig.get(uuid1);
        }
        return configList.get(0);
    }

    public static boolean setActiveConfig(String name, UUID uuid) {
        for (APIConfig cfg : configList) {
            if (cfg.model.equals(name)) {
                playerActiveConfig.put(uuid, cfg);
                return true;
            }
        }
        return false;
    }
}
