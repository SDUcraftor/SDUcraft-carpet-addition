package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import static top.sducraft.helpers.commands.easyFakePeace.easyFakePeaceCommandHelper.showFakePeaceStatus;
import static top.sducraft.util.massageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.massageComponentCreate.createDescriptionClickComponent;

public class fakepeaceEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "fakepeace";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[伪和平]", "/easycommand fakepeace","点击进入伪和平控制界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = createDescriptionClickComponent("\n[伪和平原理简介]在Minecraft中,一个维度的刷怪数量存在上限(粗略计算方法为生存模式玩家数量*70),所以只要提前准备足够数量的怪物就可以在某个维度实现'和平'的效果",
                "https://zh.minecraft.wiki/w/Tutorial:%E4%BC%AA%E5%92%8C%E5%B9%B3?variant=zh-cn",
                "点击查看伪和平原理详细介绍","注意事项:在开关伪和平前请告知其他玩家,以免发生意外,非必要不要关闭伪和平\n");
        player.displayClientMessage(component,false);
        showFakePeaceStatus(player);
    }
}
