package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.net.URISyntaxException;

import static carpet.utils.Translations.tr;

public interface IEasyCommand {

    String getCommandName();

    Component clickButton ();

    void showEasyCommandInterface (ServerPlayer player) throws URISyntaxException;

    default String getLabelText() {
        return tr("sducarpet.easycommand.dialog." + getCommandName());
    }

    default String getHoverText() {
        return tr("sducarpet.easycommand.dialog.open") + getLabelText();
    }

}