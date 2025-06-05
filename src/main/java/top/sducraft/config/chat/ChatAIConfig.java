package top.sducraft.config.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ChatAIConfig {
    public static File configFile;
    public static APIConfig config = new APIConfig();

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
                config = new Gson().fromJson(reader, APIConfig.class);
                reader.close();
            } else {
                saveConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(config, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}