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

import static top.sducraft.util.Message.translateComponent;

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
        String changer = changeTickPlayName != null ? changeTickPlayName : translateComponent("sducarpet.tickrate.someone").getString();

        MutableComponent message = Component.literal(changer).append(translateComponent(action))
                .append(translateComponent("/tick reset")
                        .withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent.RunCommand(resetCommand))
                                .withColor(ChatFormatting.AQUA)))
                .append(translateComponent("sducarpet.tickrate.resetHint").withStyle(ChatFormatting.RESET));
        player.displayClientMessage(message, false);

        boolean hasCustomMessage = playername != null && tickRateChangeMessage != null;
        if (hasCustomMessage) {
            MutableComponent customMessage = translateComponent("sducarpet.tickrate.messageFrom")
                    .append(Component.literal(playername).withStyle(ChatFormatting.YELLOW))
                    .append(translateComponent("sducarpet.tickrate.messageSeparator").withStyle(ChatFormatting.RESET))
                    .append(Component.literal(tickRateChangeMessage).withStyle(ChatFormatting.AQUA));
            player.displayClientMessage(customMessage, false);
        }
    }

    public static void sendTickRateChangeMessage(Player player) {
        if (!SDUcraftCarpetSettings.tickRateChangedMessage) return;
        MinecraftServer server = player.getServer();
        if (server == null) return;

        ServerTickRateManager tickRateManager = server.tickRateManager();

        if (tickRateManager.isSprinting()) {
            sendInfoMessage(player, "sducarpet.tickrate.action.sprinting", "/tick warp stop");
        } else if (tickRateManager.tickrate() > 20.0f) {
            sendInfoMessage(player, "sducarpet.tickrate.action.speedup", "/tick rate 20");
        } else if (tickRateManager.tickrate() < 20.0f) {
            sendInfoMessage(player, "sducarpet.tickrate.action.slowdown", "/tick rate 20");
        } else if (tickRateManager.isFrozen()) {
            sendInfoMessage(player, "sducarpet.tickrate.action.frozen", "/tick freeze");
        }
    }
}
