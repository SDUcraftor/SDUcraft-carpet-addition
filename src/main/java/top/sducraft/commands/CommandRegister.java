package top.sducraft.commands;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import top.sducraft.commands.allitemCommand.AllItemCommand;
import top.sducraft.commands.allitemCommand.DebugAllitem;
import top.sducraft.commands.easyCommand.EasyCommand;
import top.sducraft.commands.easyCommand.MachineStatusCommand;
import top.sducraft.commands.easyCommand.WarningCommand;
import top.sducraft.commands.fakePeaceCommand.FakePeaceCommand;
import top.sducraft.commands.fakePeaceCommand.SetFakePeaceCommand;
import top.sducraft.commands.finditem.FindItemAreaCommand;
import top.sducraft.commands.finditem.FindItemCommand;
import top.sducraft.commands.openAIChat.ChatCommand;
import top.sducraft.commands.setSignUrl.OpenUrlCommand;
import top.sducraft.commands.setSignUrl.SetSignUrlCommand;
import top.sducraft.commands.syncmaticaCommand.SyncmaticaCommand;
import top.sducraft.commands.tickRateChangedMessageCommand.TickRateChangeMessageCommand;
import top.sducraft.commands.tickRateChangedMessageCommand.TickResetCommand;

public class CommandRegister {
    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> TickRateChangeMessageCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> SetFakePeaceCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> FakePeaceCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> EasyCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> MachineStatusCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> TickResetCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> WarningCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> AllItemCommand.register(dispatcher)));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {DebugAllitem.register(dispatcher);}));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {SyncmaticaCommand.register(dispatcher);}));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {FindItemCommand.register(dispatcher, registryAccess);}));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {ChatCommand.register(dispatcher);}));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {SetSignUrlCommand.register(dispatcher);}));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {OpenUrlCommand.register(dispatcher);}));
        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> {FindItemAreaCommand.register(dispatcher);}));}
}
