package top.sducraft.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import top.sducraft.commands.allitemCommand.AllItemCommand;
import top.sducraft.commands.easyCommand.Easycommand;
import top.sducraft.commands.easyCommand.MachineStatusCommand;
import top.sducraft.commands.easyCommand.WarningCommand;
import top.sducraft.commands.fakePeaceCommand.FakePeaceCommand;
import top.sducraft.commands.fakePeaceCommand.SetFakePeaceCommand;
import top.sducraft.commands.tickRateChangedMessageCommand.TickRateChangeMessageCommand;
import top.sducraft.commands.tickRateChangedMessageCommand.TickResetCommand;

public class CommandRegister {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> TickRateChangeMessageCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> SetFakePeaceCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> FakePeaceCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> Easycommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> MachineStatusCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> TickResetCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> WarningCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> AllItemCommand.register(dispatcher)));
    }
}
