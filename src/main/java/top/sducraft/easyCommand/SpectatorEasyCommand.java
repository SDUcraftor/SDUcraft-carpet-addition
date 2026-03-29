package top.sducraft.easyCommand;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;
import static top.sducraft.util.Message.translateComponent;

public class SpectatorEasyCommand extends AbstractDocumentEasyCommand {
    @Override
    public String getCommandName() {
        return "spectator";
    }

    @Override
    protected List<Component> getMessages(ServerPlayer player) {
        Component intro = createDocumentHeader(
                "sducarpet.text.spectator.intro_title",
                "https://mcdreforged.com/zh-CN/plugin/gamemode",
                "sducarpet.text.spectator.click_doc",
                Component.empty()
                        .append(translateComponent("sducarpet.text.spectator.intro_body"))
                        .append(translateComponent("sducarpet.text.spectator.notice"))
        );
        Component actions = Component.empty()
                .append(createSuggestClickComponent("[切换游戏模式] ", "!!spec", "点击切换游戏模式"))
                .append(createSuggestClickComponent(" [tp]", "!!tp", null));
        return List.of(intro, actions);
    }
}
