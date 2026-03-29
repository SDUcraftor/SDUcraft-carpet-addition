package top.sducraft.easyCommand;

import net.minecraft.server.level.ServerPlayer;
import top.sducraft.util.dialog.ActionBuilder;
import top.sducraft.util.dialog.MultiActionDialogBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static top.sducraft.util.Message.sandPlayerDialog;

public class EasyCommandHelper {
    private static final List<IEasyCommand> EASY_COMMANDS = List.of(
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


    private static final Map<String, IEasyCommand> EASY_COMMANDS_BY_NAME = EASY_COMMANDS.stream()
            .collect(Collectors.toUnmodifiableMap(
                    command -> command.getCommandName().toLowerCase(Locale.ROOT),
                    Function.identity()
            ));

    public static List<IEasyCommand> getEasyCommands() {
        return EASY_COMMANDS;
    }

    public static Optional<IEasyCommand> getEasyCommand(String commandName) {
        return Optional.ofNullable(EASY_COMMANDS_BY_NAME.get(commandName.toLowerCase(Locale.ROOT)));
    }

    public static void showEasyCommandInterface(ServerPlayer player) {
        MultiActionDialogBuilder dialogBuilder = new MultiActionDialogBuilder();

        dialogBuilder.setTitle("快捷命令")
                .setColumns(3)
                .addBodyText("欢迎使用快捷命令系统，请点击下方按钮执行操作")
                .addExitButton("关闭");

        for (IEasyCommand command : EASY_COMMANDS) {
            ActionBuilder button = new ActionBuilder(command.getLabelText())
                    .withTooltip(command.getHoverText())
                    .asRunCommand("/easycommand " + command.getCommandName());

            dialogBuilder.addAction(button);
        }
        sandPlayerDialog(player, dialogBuilder);
    }
}
