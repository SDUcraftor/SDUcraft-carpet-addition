package top.sducraft.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import top.sducraft.commands.easyCommand.easycommand;
import top.sducraft.commands.fakePeaceCommand.fakePeaceCommand;
import top.sducraft.commands.fakePeaceCommand.setFakePeaceCommand;
import top.sducraft.commands.tickRateChangedMessageCommand.tickRateChangeMessageCommand;

public class commandregister {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> tickRateChangeMessageCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> setFakePeaceCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> fakePeaceCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> easycommand.register(dispatcher)));
    }
}
