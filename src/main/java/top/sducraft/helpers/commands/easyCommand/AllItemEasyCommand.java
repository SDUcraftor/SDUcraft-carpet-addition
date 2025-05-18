package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class AllItemEasyCommand implements IEasyCommand{
    @Override
    public String getCommandName() {
        return "";
    }

    @Override
    public Component clickButton() {
        return null;
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {

    }


}
