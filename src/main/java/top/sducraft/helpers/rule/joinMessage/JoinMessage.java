package top.sducraft.helpers.rule.joinMessage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.util.DelayedEvents;

import java.net.URI;

import static top.sducraft.config.rule.JoinMessageConfig.markAsSeen;
import static top.sducraft.config.rule.JoinMessageConfig.shouldShowDialog;
import static top.sducraft.helpers.commands.tickRateChangeMessage.TickRateChangeMessageCommandHelper.sendTickRateChangeMessage;
import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;
import static top.sducraft.util.Message.translateComponent;

public abstract class JoinMessage {
    public static void showJoinMessage(ServerPlayer player) {
        DelayedEvents.START_SERVER_TICK.register(9, server -> {
            player.displayClientMessage(translateComponent("sducarpet.joinmessage.prefix")
                            .append(translateComponent("sducarpet.joinmessage.link")
                                    .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.OpenUrl(new URI("https://www.sducraft.top/community/notice?id=22&header=%E5%B8%B8%E7%94%A8%E6%8C%87%E4%BB%A4%E8%AF%B4%E6%98%8E")))
                                            .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.joinmessage.linkHover")))))
                            .append(translateComponent("sducarpet.joinmessage.orUse"))
                            .append(createSuggestClickComponent(" /easycommand ", "/easycommand", null))
                            .append(translateComponent("sducarpet.joinmessage.suffix"))
                    , false);
            sendTickRateChangeMessage(player);
            if (shouldShowDialog(player)) {
                server.getCommands().performPrefixedCommand(
                        server.createCommandSourceStack(),
                        "dialog show " + player.getGameProfile().getName() + " sdu:notice"
                );
                markAsSeen(player);
            }
        });
    }
}
