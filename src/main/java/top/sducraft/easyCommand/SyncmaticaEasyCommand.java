package top.sducraft.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.displayAllItemInfo;
import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;

public class SyncmaticaEasyCommand implements IEasyCommand{
    @Override
    public String getCommandName() {
        return "syncmatica";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[材料列表助手]", "/easycommand syncmatica","材料列表助手");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[syncmatica命令简介]").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.mcmod.cn/class/6842.html"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看共享投影介绍"))))
                .append(Component.literal("""
                        Syncmatica 模组可以使你在服务器中与其他安装了 Syncmatica 模组的玩家一起共享投影
                        syncmatica命令用于在全物品中高亮对应材质
                        """));
        player.displayClientMessage(component, false);
        player.displayClientMessage(Component.literal("""
                        /syncmatica list <page> 显示当前共享投影列表
                        /syncmatica material <syncmatica> 加载指定投影的材料列表
                        /asyncmatica clear 清除当前材料列表
                        """),false);
        player.displayClientMessage(Component.literal("温馨提示:返回列表中很多元素可以通过直接 点击 获取详细信息").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)),false);
    }
}
