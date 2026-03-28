package top.sducraft.commands.easyCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.easyCommand.IEasyCommand;

import java.net.URISyntaxException;

import static top.sducraft.easyCommand.EasyCommandHelper.EASYCOMMANDS;
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
                            for (IEasyCommand command : EASYCOMMANDS) {
                                builder.suggest(command.getCommandName());
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            for (IEasyCommand command : EASYCOMMANDS) {
                                if (command.getCommandName().equalsIgnoreCase(StringArgumentType.getString(context, "option"))) {
                                    try {
                                        command.showEasyCommandInterface(context.getSource().getPlayer());
                                    } catch (URISyntaxException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return 1;
                                }
                            }
                            context.getSource().sendFailure(translateComponent("sducarpet.command.easycommand.invalidOption"));
                            return 0;
                        })
                )
        );
    }
}
