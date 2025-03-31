package top.sducraft.commands.easyCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.commands.easyCommand.IEasyCommand;
import static top.sducraft.helpers.commands.easyCommand.easyCommandHelper.EASYCOMMANDS;
import static top.sducraft.helpers.commands.easyCommand.easyCommandHelper.showEasyCommandInterface;

public class easycommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("easycommand")
                .requires(c -> SDUcraftCarpetSettings.easyCommand)
                .executes(context -> {
                    showEasyCommandInterface((context.getSource().getPlayer()));
                    return 1;
                })
                .then(Commands.argument("option", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (IEasyCommand command : EASYCOMMANDS) {
                                builder.suggest(command.getCommandName());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            for (IEasyCommand command : EASYCOMMANDS) {
                                if (command.getCommandName().equalsIgnoreCase(StringArgumentType.getString(context, "option"))) {
                                    command.showEasyCommandInterface(context.getSource().getPlayer());
                                    return 1;
                                }
                            }
                            context.getSource().sendFailure(Component.literal("无效的命令选项"));
                            return 0;
                        })
                )
        );
    }
}

