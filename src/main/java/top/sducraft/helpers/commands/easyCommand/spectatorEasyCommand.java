package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;

public class spectatorEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "spectator";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[旁观者模式]","/easycommand spectator","点击查看!!spec命令介绍");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[!!spec命令简介]").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mcdreforged.com/zh-CN/plugin/gamemode"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看!!spec命令使用文档"))))
                .append(Component.literal("一个基于MCDR的游戏模式切换插件\n"))
                .append(Component.literal("注意事项:tp指令只能在旁观者模式下使用").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        player.displayClientMessage(component, false);
        player.displayClientMessage(Component.empty()
                .append(createSuggestClickComponent("[切换游戏模式] ","!!spec","点击切换游戏模式"))
                .append(createSuggestClickComponent(" [tp]","!!tp",null)), false);
    }
}
