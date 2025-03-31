package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public interface IEasyCommand {

    String getCommandName();

    Component clickButton ();

    void showEasyCommandInterface (ServerPlayer player);
}
