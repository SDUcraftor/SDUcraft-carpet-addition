package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import static top.sducraft.util.MassageComponentCreate.*;

public class EasyPerpetualDayEasyCommand implements IEasyCommand {
    @Override
    public Component clickButton() {
            return createCommandClickComponent("[永昼机]", "/easycommand perpetualday","点击进入永昼机控制界面");
    }

    @Override
    public String getCommandName() {
        return "perpetualday";
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
            Component component = createDescriptionClickComponent("\n[永昼机简介]利用假人模拟玩家睡觉来跳过夜晚",
                    "https://www.bilibili.com/video/BV1rq4y1E7mE/?vd_source=5817b45e0aefe917b84852b8ee9019da",
                    "点击查看永昼机详细介绍","注意事项:在开关永昼机前请告知其他玩家,以免发生意外,非必要不要关闭永昼机");
            player.displayClientMessage(component,false);
            player.displayClientMessage(Component.empty().append(createSuggestClickComponent("[开启永昼机]","!!bot tags sleep spawn","点击开启永昼机")).append(createSuggestClickComponent("[关闭永昼机]","!!bot tags sleep kill","点击关闭永昼机")),false);
    }
}
