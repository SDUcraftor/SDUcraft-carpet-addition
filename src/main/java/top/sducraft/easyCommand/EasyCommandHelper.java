package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

    public static void showEasyCommandInterface(ServerPlayer player) {
        Component component1 =  Component.literal("\n欢迎使用sducrafrt快捷命令系统,以下消息中所有[]按钮均可点击\n");
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
