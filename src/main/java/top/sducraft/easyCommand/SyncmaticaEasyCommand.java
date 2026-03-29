package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static top.sducraft.util.Message.translateComponent;

public class SyncmaticaEasyCommand extends AbstractDocumentEasyCommand {
    @Override
    public String getCommandName() {
        return "syncmatica";
    }

    @Override
    protected List<Component> getMessages(ServerPlayer player) {
        Component intro = createDocumentHeader(
                "sducarpet.text.syncmatica.intro_title",
                "https://www.mcmod.cn/class/6842.html",
                "sducarpet.text.syncmatica.click_intro_doc",
                Component.empty().append(Component.literal("""
                        Syncmatica 模组可以使你在服务器中与其他安装了 Syncmatica 模组的玩家一起共享投影
                        syncmatica命令用于在全物品中高亮对应材质
                        """))
        );
        Component commands = Component.literal("""
                /syncmatica list <page> 显示当前共享投影列表
                /syncmatica material <syncmatica> 加载指定投影的材料列表
                /asyncmatica clear 清除当前材料列表
                """);
        return List.of(intro, commands, translateComponent("sducarpet.text.common.friendly_tip_click"));
    }
}
