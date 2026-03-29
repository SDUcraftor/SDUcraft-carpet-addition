package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.displayAllItemInfo;
import static top.sducraft.util.Message.translateComponent;

public class AllItemEasyCommand extends AbstractDocumentEasyCommand {
    @Override
    public String getCommandName() {
        return "allitem";
    }

    @Override
    protected List<Component> getMessages(ServerPlayer player) {
        Component intro = createDocumentHeader(
                "sducarpet.text.allitem.intro_title",
                "https://zh.minecraft.wiki/w/Tutorial:%E9%80%9A%E7%94%A8%E7%89%A9%E5%93%81%E5%88%86%E7%B1%BB%E5%99%A8?variant=zh-cn#%E5%85%A8%E7%89%A9%E5%93%81",
                "sducarpet.text.allitem.click_intro_doc",
                Component.empty().append(Component.literal("""
                        全物品是一种自动分类,储存mc中所有物品的装置
                        allitem 指令用于快速检索全物品中某物品的位置或获取某物品的信息
                        """))
        );
        Component commands = Component.literal("""
                /allitem search <item> 搜索指定物品位置(支持中英文搜索)并高亮展示,让玩家看向目标位置
                /allitem info <item> 返回指定物品的信息
                /allitem info <type>
                    lack 返回缺货物品列表
                    full 返回即将爆仓的物品列表
                    custom 返回常规物品列表
                    all 返回全部物品列表
                """);
        Component tip = translateComponent("sducarpet.text.common.friendly_tip_click");
        return List.of(intro, commands, tip);
    }

    @Override
    protected void afterMessages(ServerPlayer player) {
        displayAllItemInfo(player);
    }
}
