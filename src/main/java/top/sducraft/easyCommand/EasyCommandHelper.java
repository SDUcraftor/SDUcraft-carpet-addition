package top.sducraft.easyCommand;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.util.dialog.ActionBuilder;
import top.sducraft.util.dialog.MultiActionDialogBuilder;

import java.util.List;

public class EasyCommandHelper {
    public static List<IEasyCommand> EASYCOMMANDS =List.of(
            new MachineStatusCommand(),
            new LocEasyCommand(),
            new EasyPerpetualDayEasyCommand(),
            new FakepeaceEasyCommand(),
            new MirrorManageEasyCommand(),
            new SpectatorEasyCommand(),
            new TickRateManagerEasyCommand(),
            new WarningEasyCommand(),
            new AllItemEasyCommand(),
            new PrimeBackupEasyCommand(),
            new SyncmaticaEasyCommand()
    );

    public static JsonObject generateEasyCommandDialogJson() {
        JsonObject root = new JsonObject();

        // 1. 设置静态属性
        root.addProperty("type", "multi_action");
        root.addProperty("columns", 4);
        root.addProperty("after_action", "close");

        // 2. 设置标题
        JsonObject title = new JsonObject();
        title.addProperty("text", "快捷命令");
        root.add("title", title);

        // 3. 设置主体欢迎文本
        JsonArray bodyArray = new JsonArray();
        JsonObject bodyMessage = new JsonObject();
        bodyMessage.addProperty("type", "plain_message");
        JsonObject bodyContents = new JsonObject();
        bodyContents.addProperty("text", "欢迎使用快捷命令系统，请点击下方按钮执行操作。");
        bodyMessage.add("contents", bodyContents);
        bodyArray.add(bodyMessage);
        root.add("body", bodyArray);

        // 4. 动态生成 actions 数组
        JsonArray actionsArray = new JsonArray();
        for (IEasyCommand command : EASYCOMMANDS) {
            JsonObject actionContainer = new JsonObject();

            // 设置按钮标签
            actionContainer.addProperty("label", command.getLabelText());

            // 设置悬浮提示 (如果存在)
            if (command.getHoverText() != null && !command.getHoverText().isEmpty()) {
                JsonObject tooltip = new JsonObject();
                tooltip.addProperty("text", command.getHoverText());
                actionContainer.add("tooltip", tooltip);
            }

            // 设置点击后执行的命令
            JsonObject commandAction = new JsonObject();
            commandAction.addProperty("type", "run_command");
            commandAction.addProperty("command", "/easycommand " + command.getCommandName());
            actionContainer.add("action", commandAction);

            actionsArray.add(actionContainer);
        }
        root.add("actions", actionsArray);

        // 5. 设置退出按钮
        JsonObject exitAction = new JsonObject();
        JsonObject exitLabel = new JsonObject();
        exitLabel.addProperty("translate", "gui.cancel");
        exitAction.add("label", exitLabel);
        root.add("exit_action", exitAction);

        return root;
}


//    public static void showEasyCommandInterface(ServerPlayer player) {
//        Component component1 =  Component.literal("\n欢迎使用sducrafrt快捷命令系统,以下消息中所有[]按钮均可点击\n");
//        int i = 0;
//        for (IEasyCommand command : EASYCOMMANDS) {
//            component1 = Component.empty().append(component1).append(" ").append(command.clickButton()).append(" ") ;
//            i++;
//            if(i%4==0)
//            {
//                component1 =  Component.empty().append(component1). append("\n");
//            }
//        }
//        player.displayClientMessage(component1, false);
//    }

    public static void showEasyCommandInterface(ServerPlayer player) {
        MultiActionDialogBuilder dialogBuilder = new MultiActionDialogBuilder();


        dialogBuilder.setTitle("快捷命令")
                .setColumns(3)
                .addBodyText("欢迎使用快捷命令系统，请点击下方按钮执行操作。")
                .addExitButton("关闭");

        for (IEasyCommand command : EASYCOMMANDS) {

            ActionBuilder button = new ActionBuilder(command.getLabelText())
                    .withTooltip(command.getHoverText())
                    .asRunCommand("/easycommand " + command.getCommandName());

            dialogBuilder.addAction(button);
        }
        String jsonString = dialogBuilder.build().toString();


        String command = "dialog show " + player.getGameProfile().getName() + " " + jsonString;
        MinecraftServer server = player.getServer();
        server.getCommands().performPrefixedCommand(
                server.createCommandSourceStack(), // 使用服务器的命令源
                command
        );
    }
}
