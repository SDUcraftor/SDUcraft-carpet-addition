package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;

public class  mirrorManageEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "mirrormanager";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[镜像服管理]", "/easycommand mirrormanager","点击进入镜像服管理界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[!!msr指令简介]").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mcdreforged.com/zh-CN/plugin/mirror_server_reforged"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看MCDR的镜像服务器管理插件文档"))))
                .append(Component.literal("一个基于MCDR的镜像服务器管理插件\n"))
                .append(Component.literal("注意事项:在执行镜像服相关操作请先确认镜像服中没有人正在测试机器~").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        Component component1 = Component.empty()
                .append(createSuggestClickComponent("[开启镜像服]", "!!msr start", "点击开启镜像服"))
                .append(Component.literal(" "))
                .append(createSuggestClickComponent("[关闭镜像服]", "!!msr stop", "点击关闭镜像服"))
                .append(Component.literal(" "))
                .append(createSuggestClickComponent("[同步生存服]", "!!msr sync", "点击同步生存服"));

        player.displayClientMessage(component, false);
        player.displayClientMessage(component1, false);
    }
}
