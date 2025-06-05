package top.sducraft.helpers.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.config.chat.ChatAIConfig;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static carpet.utils.Translations.tr;

public class OpenaiChat {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Gson gson = new GsonBuilder().create();
    private static final Set<UUID> generatingPlayers = ConcurrentHashMap.newKeySet();


    public static String getCompletionWithMemory(String userPrompt, ServerPlayer player) {
        ChatAIConfig.APIConfig cfg = ChatAIConfig.config;

        ChatMemory.appendMessage(player, "user", userPrompt);

        List<ChatMemory.Message> history = ChatMemory.getHistory(player);

        JsonArray messagesJsonArray = new JsonArray();

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", cfg.systemPrompt);
        messagesJsonArray.add(systemMessage);

        for (ChatMemory.Message m : history) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.role);
            msg.addProperty("content", m.content);
            messagesJsonArray.add(msg);
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", cfg.model);
        body.add("messages", messagesJsonArray);
        body.addProperty("stream", false);
        body.addProperty("temperature", cfg.temperature);
        body.addProperty("max_tokens", cfg.maxTokens);

        String requestBody = gson.toJson(body);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cfg.apiUrl))
                .timeout(Duration.ofSeconds(30))
                .header(cfg.authorizationHeader, "Bearer " + cfg.apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return tr("sducarpet.command.chat4") + response.statusCode();
            }

            JsonObject respJson = gson.fromJson(response.body(), JsonObject.class);
            JsonObject message = respJson.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message");

            String reply = message.get("content").getAsString();

            ChatMemory.appendMessage(player, "assistant", reply);

            return reply;

        } catch (Exception e) {
            return tr("sducarpet.command.chat4") + e.getMessage();
        }
    }

    public static void tryStartChat(String prompt, ServerPlayer player) {
        UUID id = player.getUUID();
        if (generatingPlayers.contains(id)) {
            player.displayClientMessage(Component.literal(tr("sducarpet.command.chat3")), false);
            return;
        }

        generatingPlayers.add(id);
        player.displayClientMessage(Component.literal("\nuser:\n" + prompt), false);

        new Thread(() -> {
            try {
                String reply = getCompletionWithMemory(prompt, player);
                player.getServer().execute(() -> {
                    player.displayClientMessage(Component.literal("deepseek:\n" + reply), false);
                });
            } finally {
                generatingPlayers.remove(id);
            }
        }).start();
    }
}

