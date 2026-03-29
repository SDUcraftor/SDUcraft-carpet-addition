package top.sducraft.easyCommand;

import net.minecraft.server.level.ServerPlayer;

import static carpet.utils.Translations.tr;

public interface IEasyCommand {

    String getCommandName();

    void showEasyCommandInterface(ServerPlayer player);

    default String getLabelText() {
        return tr("sducarpet.easycommand.dialog." + getCommandName());
    }

    default String getHoverText() {
        return tr("sducarpet.easycommand.dialog.open") + getLabelText();
    }

}
