package top.sducraft.helpers.commands.tickRateChangeMessage;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.world.entity.player.Player;
import top.sducraft.SDUcraftCarpetSettings;

public class TickRateChangeMessageCommandHelper {

    public static String playername = null;
    public static String tickRateChangeMessage = null;
    public static String changeTickPlayName = null;

    public static void resetTickRateChangeMessage() {
        tickRateChangeMessage = null;
        playername = null;
        changeTickPlayName = null;
    }

    private static void sendInfoMessage(Player player, String action, String resetCommand) {
        String changer = changeTickPlayName != null ? changeTickPlayName : "有人";

        MutableComponent message = Component.literal(changer + action)
                .append(Component.literal("/tick reset")
                        .withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent.RunCommand(resetCommand))
                                .withColor(ChatFormatting.AQUA)))
                .append(Component.literal(" 指令恢复正常游戏速度").withStyle(ChatFormatting.RESET));
        player.displayClientMessage(message, false);

        boolean hasCustomMessage = playername != null && tickRateChangeMessage != null;
        if (hasCustomMessage) {
            MutableComponent customMessage = Component.literal("来自 ").append(Component.literal(playername).withStyle(ChatFormatting.YELLOW)).append(Component.literal(" 的留言: ").withStyle(ChatFormatting.RESET)).append(Component.literal(tickRateChangeMessage).withStyle(ChatFormatting.AQUA));
            player.displayClientMessage(customMessage, false);
        }
    }

    public static void sendTickRateChangeMessage(Player player) {
        if (!SDUcraftCarpetSettings.tickRateChangedMessage) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerTickRateManager tickRateManager = server.tickRateManager();

        if (tickRateManager.isSprinting()) {
            sendInfoMessage(player, "加速了游戏，可以使用 ", "/tick warp stop");
        } else if (tickRateManager.tickrate() > 20.0f) {
            sendInfoMessage(player, "加速了游戏，可以使用 ", "/tick rate 20");
        } else if (tickRateManager.tickrate() < 20.0f) {
            sendInfoMessage(player, " 减速了游戏，可以使用 ", "/tick rate 20");
        } else if (tickRateManager.isFrozen()) {
            sendInfoMessage(player, "冻结了游戏，可以使用 ", "/tick freeze");
        }
    }
}