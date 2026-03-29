package top.sducraft.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static top.sducraft.util.Message.translateComponent;

public class LocEasyCommand extends AbstractDocumentEasyCommand {
    @Override
    public String getCommandName() {
        return "locationmarker";
    }

    @Override
    protected List<Component> getMessages(ServerPlayer player) {
        Component intro = createDocumentHeader(
                "sducarpet.text.loc.intro_title",
                "https://mcdreforged.com/zh-CN/plugin/location_marker",
                "sducarpet.text.loc.click_doc",
                translateComponent("sducarpet.text.loc.intro_body")
        );
        Component usage = translateComponent("sducarpet.text.loc.input_command_prefix")
                .append(translateComponent("!!loc").withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent.SuggestCommand("!!loc"))
                        .withColor(ChatFormatting.AQUA)))
                .append(translateComponent("sducarpet.text.loc.details_suffix"));
        return List.of(intro, usage);
    }

}
