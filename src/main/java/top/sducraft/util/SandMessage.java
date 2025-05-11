package top.sducraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.sducraft.helpers.visualizers.HopperCooldownVisualizing;

import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

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
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                for(ServerPlayer player : server.getPlayerList().getPlayers()){
                    sandCustomMessage(player, message, color);
                }
                timer.cancel();}
        }, delay);
    }
}
