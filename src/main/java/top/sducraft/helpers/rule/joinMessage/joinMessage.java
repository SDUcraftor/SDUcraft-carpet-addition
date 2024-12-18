package top.sducraft.helpers.rule.joinMessage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import java.util.Timer;
import java.util.TimerTask;

import static top.sducraft.util.massageComponentCreate.createSuggestClickComponent;
import static top.sducraft.helpers.commands.tickRateChangeMessage.tickRateChangeMessageCommandHelper.sendTickRateChangeMessage;

public abstract class joinMessage {
    public static void showJoinMessage(ServerPlayer player) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                player.displayClientMessage(Component.literal("强烈建议新玩家先阅读SDUcraft常用命令")
                        .append(Component.literal("[点我转跳]")
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.sducraft.top/community/notice?id=22&header=%E5%B8%B8%E7%94%A8%E6%8C%87%E4%BB%A4%E8%AF%B4%E6%98%8E"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看SDUcraft常用命令")))))
                        .append(Component.literal("或使用"))
                        .append(createSuggestClickComponent(" /easycommand ","/easycommand",null))
                        .append(Component.literal("来获取良好的游戏内指令体验"))
                        ,false);
                sendTickRateChangeMessage(player);
                timer.cancel();}}, 500);
    }
}
