package top.sducraft.commands.easyCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import top.sducraft.SDUcraftCarpetSettings;

import static top.sducraft.easyCommand.EasyCommandHelper.getEasyCommand;
import static top.sducraft.easyCommand.EasyCommandHelper.getEasyCommands;
import static top.sducraft.easyCommand.EasyCommandHelper.showEasyCommandInterface;
import static top.sducraft.util.Message.translateComponent;

public class EasyCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("easycommand")
                .requires(c -> SDUcraftCarpetSettings.easyCommand)
                .executes(context -> {
                    showEasyCommandInterface((context.getSource().getPlayer()));
                    return 1;
                })
                .then(Commands.argument("option", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (var command : getEasyCommands()) {
                                builder.suggest(command.getCommandName());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            String option = StringArgumentType.getString(context, "option");
                            var command = getEasyCommand(option);
                            if (command.isPresent()) {
                                command.get().showEasyCommandInterface(context.getSource().getPlayer());
                                return 1;
                            }
                            context.getSource().sendFailure(translateComponent("sducarpet.command.easycommand.invalidOption"));
                            return 0;
                        })
                )
        );
    }
}
