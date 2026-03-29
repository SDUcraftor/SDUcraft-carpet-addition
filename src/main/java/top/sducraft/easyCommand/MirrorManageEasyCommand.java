package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;
import static top.sducraft.util.Message.translateComponent;

public class MirrorManageEasyCommand extends AbstractDocumentEasyCommand {
    @Override
    public String getCommandName() {
        return "mirrormanager";
    }

    @Override
    protected List<Component> getMessages(ServerPlayer player) {
        Component intro = createDocumentHeader(
                "sducarpet.text.mirror.intro_title",
                "https://mcdreforged.com/zh-CN/plugin/mirror_server_reforged",
                "sducarpet.text.mirror.click_doc",
                Component.empty()
                        .append(translateComponent("sducarpet.text.mirror.intro_body"))
                        .append(translateComponent("sducarpet.text.mirror.notice"))
        );
        Component actions = Component.empty()
                .append(createSuggestClickComponent("[开启镜像服]", "!!msr start", "点击开启镜像服"))
                .append(translateComponent(" "))
                .append(createSuggestClickComponent("[关闭镜像服]", "!!msr stop", "点击关闭镜像服"))
                .append(translateComponent(" "))
                .append(createSuggestClickComponent("[同步生存服]", "!!msr sync", "点击同步生存服"));
        return List.of(intro, actions);
    }
}
