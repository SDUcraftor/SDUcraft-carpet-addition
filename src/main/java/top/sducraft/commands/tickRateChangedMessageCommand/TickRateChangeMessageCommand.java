package top.sducraft.commands.tickRateChangedMessageCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.commands.tickRateChangeMessage.TickRateChangeMessageCommandHelper;
import java.util.Objects;

import static carpet.utils.Translations.tr;
import static net.minecraft.commands.Commands.argument;

public class TickRateChangeMessageCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("leavemassage")
                .requires(c -> SDUcraftCarpetSettings.tickRateChangedMessage)
                .then(argument("string",StringArgumentType.greedyString())
                .executes((commandContext) -> {
                        TickRateChangeMessageCommandHelper.tickRateChangeMessage=StringArgumentType.getString(commandContext, "string");
                        ServerPlayer player = commandContext.getSource().getPlayerOrException();
                        TickRateChangeMessageCommandHelper.playername = player.getName().getString();
                        if(Objects.equals(TickRateChangeMessageCommandHelper.tickRateChangeMessage, "clear")){
                        TickRateChangeMessageCommandHelper.resetTickRateChangeMessage();
                        commandContext.getSource().sendSuccess(()->Component.literal(tr("sducarpet.easycommand.tickRateChangeMessageCommand1")),true);
                      }
                        else {
                            commandContext.getSource().sendSuccess(()->Component.literal(tr("sducarpet.easycommand.tickRateChangeMessageCommand2")+StringArgumentType.getString(commandContext, "string")),true);
                        }
                    return 1;
                })));
    }
}
