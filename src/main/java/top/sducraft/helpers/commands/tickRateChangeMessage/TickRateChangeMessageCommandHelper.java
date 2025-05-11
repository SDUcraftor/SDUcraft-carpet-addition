package top.sducraft.helpers.commands.tickRateChangeMessage;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.world.entity.player.Player;
import top.sducraft.SDUcraftCarpetSettings;

public class TickRateChangeMessageCommandHelper {

    public static String playername = null;
    public static String tickRateChangeMessage = null;

    public static void resetTickRateChangeMessage() {
        tickRateChangeMessage = null;
        playername = null;
    }

    public static void sendTickRateChangeMessage(Player player) {
        if (SDUcraftCarpetSettings.tickRateChangedMessage) {
            MinecraftServer server = player.getServer();
            if (server != null) {
                ServerTickRateManager tickRateManager = server.tickRateManager();
                boolean bl = playername != null && tickRateChangeMessage != null;
                if (tickRateManager.isSprinting()) {
                    player.displayClientMessage(Component.literal("当前游戏加速中，可以使用 ").append(Component.literal("/tick reset").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tick warp stop")).withColor(ChatFormatting.AQUA))).append(Component.literal(" 指令恢复正常游戏速度").withStyle(ChatFormatting.RESET)), false);
                    if (bl) {
                        player.displayClientMessage(Component.literal("来自 ").append(Component.literal(playername).withStyle(ChatFormatting.YELLOW)).append(Component.literal("的留言:").withStyle(ChatFormatting.RESET)).append(Component.literal(tickRateChangeMessage).withStyle(ChatFormatting.AQUA)), false);
                    }
                } else if (tickRateManager.tickrate() != 20.0) {
                    player.displayClientMessage(Component.literal("当前游戏加速中，可以使用 ").append(Component.literal("/tick reset").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tick rate 20")).withColor(ChatFormatting.AQUA))).append(Component.literal(" 指令恢复正常游戏速度").withStyle(ChatFormatting.RESET)), false);
                    if (bl) {
                        player.displayClientMessage(Component.literal("来自 ").append(Component.literal(playername).withStyle(ChatFormatting.YELLOW)).append(Component.literal("的留言:").withStyle(ChatFormatting.RESET)).append(Component.literal(tickRateChangeMessage).withStyle(ChatFormatting.AQUA)), false);
                    }
                } else if (tickRateManager.isFrozen()) {
                    player.displayClientMessage(Component.literal("当前游戏冻结中，可以使用 ").append(Component.literal("/tick reset").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tick freeze")).withColor(ChatFormatting.AQUA))).append(Component.literal(" 指令恢复正常游戏速度").withStyle(ChatFormatting.RESET)), false);
                    if (bl) {
                        player.displayClientMessage(Component.literal("来自 ").append(Component.literal(playername).withStyle(ChatFormatting.YELLOW)).append(Component.literal("的留言:").withStyle(ChatFormatting.RESET)).append(Component.literal(tickRateChangeMessage).withStyle(ChatFormatting.AQUA)), false);
                    }
                }
            }
        }
    }
}