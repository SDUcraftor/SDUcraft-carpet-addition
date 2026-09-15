package top.sducraft.util;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.util.dialog.DialogBuilder;

import static carpet.utils.Translations.tr;


public abstract class Message {
    public static void sandCustomMessage(ServerPlayer player, String message, ChatFormatting color) {
        player.sendSystemMessage(translateComponent(message).withStyle(color));
    }

    public static void sandAllPlayerCustomMessage(MinecraftServer server, String message, ChatFormatting color) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sandCustomMessage(player, message, color);
        }
    }

    public static void sandAllPlayerCustomMessage(MinecraftServer server, String message, ChatFormatting color, int delay) {
        DelayedEvents.START_SERVER_TICK.register(delay, server1 -> {
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        sandCustomMessage(player, message, color);
                    }
                }
        );
    }

    public static void sandPlayerDialog(ServerPlayer player, DialogBuilder<?> dialogBuilder) {
        if (player instanceof EntityPlayerMPFake) return;

        String jsonString = dialogBuilder.build().toString();
        //#if MC >= 260100
        String command = "dialog show " + player.getGameProfile().name() + " " + jsonString;
        //#else
        //$$ String command = "dialog show " + player.getGameProfile().getName() + " " + jsonString;
        //#endif
        MinecraftServer server = player.level().getServer();
        if (server != null) {
            server.getCommands().performPrefixedCommand(
                    server.createCommandSourceStack(),
                    command
            );
        }
    }

    public static MutableComponent translateComponent(String text) {
        return Component.literal(tr(text));
    }

//    public static void sandPlayerCustomMessage(ServerPlayer player, List component,String changeCommand ,int page) {
//
//
//    }
}
