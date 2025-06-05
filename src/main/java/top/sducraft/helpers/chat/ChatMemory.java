package top.sducraft.helpers.chat;

import net.minecraft.server.level.ServerPlayer;

import java.util.*;

public class ChatMemory {
    private static final Map<UUID, LinkedList<Message>> memory = new HashMap<>();
    private static final int MAX_HISTORY = 3;

    public static class Message {
        public String role;
        public String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public static void appendMessage(ServerPlayer player, String role, String content) {
        memory.computeIfAbsent(player.getUUID(), k -> new LinkedList<>());
        LinkedList<Message> history = memory.get(player.getUUID());

        history.addLast(new Message(role, content));
        if (history.size() > MAX_HISTORY) {
            history.removeFirst();
        }
    }

    public static List<Message> getHistory(ServerPlayer player) {
        return memory.getOrDefault(player.getUUID(), new LinkedList<>());
    }

    public static void clear(ServerPlayer player) {
        memory.remove(player.getUUID());
    }
}
