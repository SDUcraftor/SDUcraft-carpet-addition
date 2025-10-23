package top.sducraft.easyCommand;

import net.minecraft.server.level.ServerPlayer;
import top.sducraft.util.dialog.ActionBuilder;
import top.sducraft.util.dialog.MultiActionDialogBuilder;

import java.util.List;

import static top.sducraft.util.Message.sandPlayerDialog;

public class EasyCommandHelper {
    public static List<IEasyCommand> EASYCOMMANDS =List.of(
            new MachineStatusEasyCommand(),
            new FindItemEasyCommand(),
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
                .addBodyText("欢迎使用快捷命令系统，请点击下方按钮执行操作")
                .addExitButton("关闭");

        for (IEasyCommand command : EASYCOMMANDS) {

            ActionBuilder button = new ActionBuilder(command.getLabelText())
                    .withTooltip(command.getHoverText())
                    .asRunCommand("/easycommand " + command.getCommandName());

            dialogBuilder.addAction(button);
        }
        sandPlayerDialog(player, dialogBuilder);
    }
}
