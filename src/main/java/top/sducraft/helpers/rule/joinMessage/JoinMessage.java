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

public abstract class JoinMessage {
    public static void showJoinMessage(ServerPlayer player) {
        DelayedEvents.START_SERVER_TICK.register(9, server -> {
            player.displayClientMessage(Component.literal("强烈建议新玩家先阅读SDUcraft常用命令")
                            .append(Component.literal("[点我转跳]")
                                    .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.OpenUrl(new URI("https://www.sducraft.top/community/notice?id=22&header=%E5%B8%B8%E7%94%A8%E6%8C%87%E4%BB%A4%E8%AF%B4%E6%98%8E")))
                                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击查看SDUcraft常用命令")))))
                            .append(Component.literal("或使用"))
                            .append(createSuggestClickComponent(" /easycommand ", "/easycommand", null))
                            .append(Component.literal("""
                                    来获取良好的游戏内指令体验
                                    新功能,使用/finditem <物品id> <范围> 可以搜索范围内的物品
                                    """))
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
