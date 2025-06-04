package top.sducraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;


public class SandMessage {
    public static void sandCustomMessage(ServerPlayer player, String message, ChatFormatting color) {
            player.displayClientMessage(Component.literal(message).withStyle(color),false);
    }

    public static void sandAllPlayerCustomMessage(MinecraftServer server, String message, ChatFormatting color) {
        for(ServerPlayer player : server.getPlayerList().getPlayers()){
            sandCustomMessage(player, message, color);
        }
    }

    public static void sandAllPlayerCustomMessage(MinecraftServer server, String message, ChatFormatting color,int delay) {
        DelayedEvents.START_SERVER_TICK.register(delay, server1 -> {
                for(ServerPlayer player : server.getPlayerList().getPlayers()){
                    sandCustomMessage(player, message, color);
                }}
        );
    }

//    public static void sandPlayerCustomMessage(ServerPlayer player, List component,String changeCommand ,int page) {
//
//
//    }
}
