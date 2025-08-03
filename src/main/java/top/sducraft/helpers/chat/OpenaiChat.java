package top.sducraft.helpers.chat;

import com.google.gson.*;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.config.chat.ChatAIConfig;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import static carpet.utils.Translations.tr;

public class OpenaiChat {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Gson gson = new GsonBuilder().create();
    private static final Set<UUID> generatingPlayers = ConcurrentHashMap.newKeySet();

    public static void tryStartChat(String prompt, ServerPlayer player) {
        UUID id = player.getUUID();
        if (generatingPlayers.contains(id)) {
            player.displayClientMessage(Component.literal(tr("sducarpet.command.chat3")), false);
            return;
        }

        generatingPlayers.add(id);

        Thread.startVirtualThread(() -> {
            try {
                streamChatWithMemory(prompt, player);
            } finally {
                generatingPlayers.remove(id);
            }
        });
    }

    private static void streamChatWithMemory(String userPrompt, ServerPlayer player) {
        ChatAIConfig.APIConfig cfg = ChatAIConfig.getActiveConfig(player.getUUID());
        boolean wantThink = userPrompt.contains("-think");
        userPrompt = userPrompt.replace("-think", "").trim();
        boolean nosystemprompt = userPrompt.contains("-nosystemprompt");
        userPrompt = userPrompt.replace("-nosystemprompt", "").trim();
        ChatMemory.appendMessage(player, "user", userPrompt);
        List<ChatMemory.Message> history = ChatMemory.getHistory(player);
        player.displayClientMessage(Component.literal("\nuser:\n" + userPrompt), false);

        JsonArray messagesJsonArray = new JsonArray();
        JsonObject systemMessage = new JsonObject();
        if(!nosystemprompt) {
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", cfg.systemPrompt);
            messagesJsonArray.add(systemMessage);

        }

        for (ChatMemory.Message m : history) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.role);
            msg.addProperty("content", m.content);
            messagesJsonArray.add(msg);
        }

        JsonObject body = new JsonObject();
        body.addProperty("model", cfg.model);
        body.add("messages", messagesJsonArray);
        body.addProperty("stream", true);
        body.addProperty("temperature", cfg.temperature);
        body.addProperty("max_tokens", cfg.maxTokens);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(cfg.apiUrl))
                .timeout(Duration.ofSeconds(60))
                .header(cfg.authorizationHeader, "Bearer " + cfg.apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body), StandardCharsets.UTF_8))
                .build();

        StringBuilder fullReply = new StringBuilder();
        StringBuilder reasoningBuffer = new StringBuilder();
        StringBuilder answerBuffer = new StringBuilder();
        boolean inReasoning = false;

        try {
            player.displayClientMessage(Component.literal("\nresponse:\n" + userPrompt), false);
            HttpResponse<java.io.InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) continue;
                String jsonLine = line.substring(6).trim();
                if (jsonLine.equals("[DONE]")) break;

                JsonObject obj = JsonParser.parseString(jsonLine).getAsJsonObject();
                JsonObject delta = obj.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("delta");

                if (delta.has("content") && !delta.get("content").isJsonNull()) {
                    String content = delta.get("content").getAsString();
                    fullReply.append(content);

                    if (content.contains("<think>")) {
                        inReasoning = true;
                        content = content.replace("<think>", "");
                        if(wantThink)player.displayClientMessage(Component.literal("\nthink:").withColor(0x808080), false);
                    }
                    if (content.contains("</think>")) {
                        inReasoning = false;
                        content = content.replace("</think>", "");
                        player.displayClientMessage(Component.literal("\nanswer:"), false);
                    }

                    if (inReasoning) {
                        reasoningBuffer.append(content);
                        if (wantThink && content.matches(".*[。.!？?]\\s*$") ) {
                            String chunk = reasoningBuffer.toString();
                            reasoningBuffer.setLength(0);
                            player.getServer().execute(() ->
                                    player.displayClientMessage(Component.literal(chunk.trim()).withColor(0x808080), false)
                            );
                        }
                    } else {
                        answerBuffer.append(content);
                        if (content.matches(".*[。.!？?]\\s*$")) {
                            String chunk = answerBuffer.toString();
                            answerBuffer.setLength(0);
                            player.getServer().execute(() ->
                                    player.displayClientMessage(Component.literal(chunk.trim()), false)
                            );
                        }
                    }
                }
            }

            ChatMemory.appendMessage(player, "assistant", fullReply.toString());

        } catch (Exception e) {
            player.getServer().execute(() ->
                    player.displayClientMessage(Component.literal(tr("sducarpet.command.chat4") + ": " + e.getMessage()), false)
            );
        }
    }


    public static CompletableFuture<Suggestions> suggestArgument(SuggestionsBuilder builder) {
        String suggestions = builder.getInput().replaceAll("/chat ", "");
        if (!builder.getInput().contains("-think")){
            builder.suggest("-think " + suggestions);
        }
        if (!builder.getInput().contains("-nosystemprompt")){
            builder.suggest("-nosystemprompt "  + suggestions);
        }
        if (!builder.getInput().contains("-think") &&  !builder.getInput().contains("-nosystemprompt")){
            builder.suggest("-nosystemprompt -think " + suggestions );
        }

        return builder.buildFuture();
    }

}

