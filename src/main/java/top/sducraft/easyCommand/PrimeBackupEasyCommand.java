package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static top.sducraft.util.Message.translateComponent;

public class PrimeBackupEasyCommand extends AbstractDocumentEasyCommand {
    @Override
    public String getCommandName() {
        return "primebackup";
    }

    @Override
    protected List<Component> getMessages(ServerPlayer player) {
        Component intro = createDocumentHeader(
                "sducarpet.text.primebackup.intro_title",
                "https://tisunion.github.io/PrimeBackup/zh/",
                "sducarpet.text.primebackup.click_doc",
                Component.empty().append(Component.literal("""
                        一个强大的 MCDR 备份插件，一套先进的 Minecraft 存档备份解决方案
                        """))
        );
        return List.of(intro, translateComponent("sducarpet.text.primebackup.more_info"));
    }
}
