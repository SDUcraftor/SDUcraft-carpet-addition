package top.sducraft.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.displayAllItemInfo;
import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;

public class AllItemEasyCommand implements IEasyCommand{
    @Override
    public String getCommandName() {
        return "allitem";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[全物品助手]", "/easycommand allitem","点击进入全物品助手");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[全物品简介]").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://zh.minecraft.wiki/w/Tutorial:%E9%80%9A%E7%94%A8%E7%89%A9%E5%93%81%E5%88%86%E7%B1%BB%E5%99%A8?variant=zh-cn#%E5%85%A8%E7%89%A9%E5%93%81"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看全物品简介介绍"))))
                .append(Component.literal("""
                        全物品是一种自动分类,储存mc中所有物品的装置
                        allitem 指令用于快速检索全物品中某物品的位置或获取某物品的信息
                        """));
        player.displayClientMessage(component, false);
        player.displayClientMessage(Component.literal("""
                        /allitem search <item> 搜索指定物品位置(支持中英文搜索)并高亮展示,让玩家看向目标位置
                        /allitem info <item> 返回指定物品的信息
                        /allitem info <type>
                            lack 返回缺货物品列表
                            full 返回即将爆仓的物品列表
                            custom 返回常规物品列表
                            all 返回全部物品列表
                        """),false);
        player.displayClientMessage(Component.literal("温馨提示:返回列表中很多元素可以通过直接 点击 获取详细信息").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)),false);
        displayAllItemInfo(player);
    }
}
