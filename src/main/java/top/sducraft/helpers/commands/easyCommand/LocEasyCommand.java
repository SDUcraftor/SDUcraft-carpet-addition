package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;

public class LocEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "locationmarker";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[服务器路标]", "/easycommand locationmarker","点击进入服务器路标界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[!!loc指令简介]").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mcdreforged.com/zh-CN/plugin/location_marker"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看Location Marker插件介绍"))))
                .append(Component.literal("一个基于MCDR的服务器路标插件"));
        player.displayClientMessage(component, false);
        player.displayClientMessage(Component.literal("输入指令 ")
                .append(Component.literal("!!loc").withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "!!loc"))
                        .withColor(ChatFormatting.AQUA)))
                .append(" 以获取详细信息"),false);
    }

}
