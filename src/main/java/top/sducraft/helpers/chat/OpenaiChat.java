package top.sducraft.helpers.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import okhttp3.*;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.config.chat.ChatAIConfig;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static carpet.utils.Translations.tr;

public class OpenaiChat {

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();;
    private static final Gson gson = new GsonBuilder().create();
    private static final Set<UUID> generatingPlayers = new HashSet<>();

    public static String getCompletionWithMemory(String userPrompt, ServerPlayer player) {
        ChatAIConfig.APIConfig cfg = ChatAIConfig.config;

        ChatMemory.appendMessage(player, "user", userPrompt);

        List<ChatMemory.Message> history = ChatMemory.getHistory(player);

        List<ChatMemory.Message> messages = new ArrayList<>();
        messages.add(new ChatMemory.Message("system", cfg.systemPrompt));
        messages.addAll(history);

        Map<String, Object> body = new HashMap<>();
        body.put("model", cfg.model);
        body.put("messages", messages);
        body.put("stream", false);
        body.put("temperature", cfg.temperature);
        body.put("max_tokens", cfg.maxTokens);

        String json = gson.toJson(body);

        Request request = new Request.Builder()
                .url(cfg.apiUrl)
                .addHeader(cfg.authorizationHeader, "Bearer " + cfg.apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return tr("sducarpet.command.chat4") + response.code();
            }

            String respBody = response.body().string();
            JsonObject respJson = gson.fromJson(respBody, JsonObject.class);
            JsonObject message = respJson
                    .getAsJsonArray("choices").get(0)
                    .getAsJsonObject().getAsJsonObject("message");

            String reply = message.get("content").getAsString();

            ChatMemory.appendMessage(player, "assistant", reply);
            return reply;

        } catch (Exception e) {
            return tr("sducarpet.command.chat4")  + e.getMessage();
        }
    }

    public static void tryStartChat(String prompt, ServerPlayer player) {
        UUID id = player.getUUID();
        if (generatingPlayers.contains(id)) {
            player.displayClientMessage(Component.literal(tr("sducarpet.command.chat3")),false);
            return;
        }

        generatingPlayers.add(id);
        player.displayClientMessage(Component.literal("\nuser:\n"+prompt),false);
        new Thread(() -> {
            try {
                String reply = getCompletionWithMemory(prompt, player);
                player.displayClientMessage(Component.literal("deepseek:\n"+reply),false);
            } finally {
                generatingPlayers.remove(id);
            }
        }).start();
    }

}

