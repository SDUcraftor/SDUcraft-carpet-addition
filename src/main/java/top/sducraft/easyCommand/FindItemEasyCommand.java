package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.util.dialog.ActionBuilder;
import top.sducraft.util.dialog.MultiActionDialogBuilder;

import java.net.URISyntaxException;

import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.Message.sandPlayerDialog;

public class FindItemEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "finditem";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[物品查找]", "/easycommand finditem", "点击打开物品查找界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) throws URISyntaxException {
        MultiActionDialogBuilder dialogBuilder = new MultiActionDialogBuilder();

        dialogBuilder.setTitle("finditem指令介绍")
                .setColumns(2) // 让两个按钮并排显示
                .addBodyText("使用/finditem <物品id><半径> 指令来查找在指定半径内查找特定物品的位置，包括容器和掉落物。")
                .addBodyText("推荐在命令中使用,可以自动补全(对话框不支持自动补全)")
                .addExitButton("关闭");

        dialogBuilder.addTextInput("item_to_find", "物品ID (例如: diamond)");
        dialogBuilder.addTextInput("search_radius", "半径 (1-256)");

        ActionBuilder findDefaultButton = new ActionBuilder("查找 (默认半径128)")
                .withTooltip("仅使用上方输入的物品ID进行查找")
                .asDynamicRunCommand("/finditem $(item_to_find)");

        ActionBuilder findCustomButton = new ActionBuilder("查找 (自定义半径)")
                .withTooltip("使用上方输入的物品ID和自定义半径进行查找")
                .asDynamicRunCommand("/finditem $(item_to_find) $(search_radius)");

        dialogBuilder.addAction(findDefaultButton);
        dialogBuilder.addAction(findCustomButton);

        sandPlayerDialog(player, dialogBuilder);
    }
}