package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.net.URISyntaxException;

public interface IEasyCommand {

    String getCommandName();

    Component clickButton ();

    void showEasyCommandInterface (ServerPlayer player) throws URISyntaxException;
}