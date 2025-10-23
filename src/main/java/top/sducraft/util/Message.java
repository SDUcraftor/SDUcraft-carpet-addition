package top.sducraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.util.dialog.DialogBuilder;

import static carpet.utils.Translations.tr;


public class Message {
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

    public static void sandPlayerDialog(ServerPlayer player, DialogBuilder<?> dialogBuilder) {
        String jsonString = dialogBuilder.build().toString();
        String command = "dialog show " + player.getGameProfile().getName() + " " + jsonString;
        MinecraftServer server = player.getServer();
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack(),
                command
        );
    }

    public static Component translateComponent (String text) {
        return Component.literal(tr(text));
    }

//    public static void sandPlayerCustomMessage(ServerPlayer player, List component,String changeCommand ,int page) {
//
//
//    }
}
