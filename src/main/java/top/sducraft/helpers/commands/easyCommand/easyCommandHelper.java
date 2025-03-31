package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;

public class easyCommandHelper {
    public static List<IEasyCommand> EASYCOMMANDS =List.of(
            new machineStatusCommand (),
            new locEasyCommand(),
            new easyPerpetualDayEasyCommand(),
            new fakepeaceEasyCommand(),
            new mirrorManageEasyCommand(),
            new spectatorEasyCommand(),
            new tickRateManagerEasyCommand()
    );

    public static void showEasyCommandInterface(ServerPlayer player) {
        Component component1 =  Component.literal("\n欢迎使用sducrafr快捷命令系统,以下消息中所有[]按钮均可点击\n");
        int i = 0;
        for (IEasyCommand command : EASYCOMMANDS) {
            component1 = Component.empty().append(component1).append(" ").append(command.clickButton()).append(" ") ;
            i++;
            if(i%4==0)
            {
                component1 =  Component.empty().append(component1). append("\n");
            }
        }
        player.displayClientMessage(component1, false);
    }
}
