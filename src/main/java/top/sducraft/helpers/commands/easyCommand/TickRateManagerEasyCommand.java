package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;
import java.util.Objects;

import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;

public class TickRateManagerEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "tickratemanager";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[游戏速度控制]", "/easycommand tickratemanager","点击进入游戏界面控制界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[tick命令简介]").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://zh.minecraft.wiki/w/%E5%91%BD%E4%BB%A4/tick"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看tick命令完整介绍"))))
                .append(Component.literal("Minecraft的绝大多数计算逻辑都在一个游戏循环内执行，执行一次这个循环就被称为执行了一次游戏刻(GameTick)作为单位时缩写为gt.在默认情况下,每秒会进行20个游戏刻,每秒最多运行游戏刻的次数可以使用tick指令进行修改.特别的,SDUcraft中开启了tickcommandcarpet化,这将使得加速游戏时的命令变为/tick warp\n"))
                .append(Component.literal("注意事项:在更改游戏速度前请先与其他玩家沟通,在更改游戏速度后可以使用").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
                .append(Component.literal(" /leavemessage ").withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/leavemessage"))
                        .withColor(ChatFormatting.AQUA)))
                .append(Component.literal("指令进行留言")).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        ServerTickRateManager tickRateManager = Objects.requireNonNull(player.getServer()).tickRateManager();
        Component tickrate = Component.literal("当前游戏速度:")
                .append(Component.literal(String.valueOf(tickRateManager.tickrate()))
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)).append(Component.literal("gt/s")));

        player.displayClientMessage(component, false);
        player.displayClientMessage(tickrate, false);
        player.displayClientMessage(Component.empty()
                .append(createSuggestClickComponent("[加速游戏]","/tick warp","使用/tick warp <time>来加速指定时间内的游戏速度,默认单位为gt"))
                .append(createSuggestClickComponent("[暂停游戏]","/tick freeze","冻结当前游戏"))
                .append(createSuggestClickComponent("[一键恢复正常游戏速度]","/tick reset","重置游戏速度")), false);
    }
}
