package top.sducraft.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.net.URISyntaxException;

import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.Message.translateComponent;
import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;

public class MirrorManageEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "mirrormanager";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[镜像服管理]", "/easycommand mirrormanager", "点击进入镜像服管理界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) throws URISyntaxException {
        Component component = translateComponent("sducarpet.text.mirror.intro_title").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent.OpenUrl(new URI("https://mcdreforged.com/zh-CN/plugin/mirror_server_reforged")))
                        .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.text.mirror.click_doc"))))
                .append(translateComponent("sducarpet.text.mirror.intro_body"))
                .append(translateComponent("sducarpet.text.mirror.notice").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        Component component1 = Component.empty()
                .append(createSuggestClickComponent("[开启镜像服]", "!!msr start", "点击开启镜像服"))
                .append(translateComponent(" "))
                .append(createSuggestClickComponent("[关闭镜像服]", "!!msr stop", "点击关闭镜像服"))
                .append(translateComponent(" "))
                .append(createSuggestClickComponent("[同步生存服]", "!!msr sync", "点击同步生存服"));

        player.displayClientMessage(component, false);
        player.displayClientMessage(component1, false);
    }
}
