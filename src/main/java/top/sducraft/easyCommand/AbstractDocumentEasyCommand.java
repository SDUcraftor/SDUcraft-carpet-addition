package top.sducraft.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.util.List;

import static top.sducraft.util.Message.translateComponent;

public abstract class AbstractDocumentEasyCommand implements IEasyCommand {

    @Override
    public final void showEasyCommandInterface(ServerPlayer player) {
        for (Component message : getMessages(player)) {
            player.displayClientMessage(message, false);
        }
        afterMessages(player);
    }

    protected abstract List<Component> getMessages(ServerPlayer player);

    protected void afterMessages(ServerPlayer player) {
    }

    protected Component createDocumentHeader(String titleKey, String url, String hoverTextKey, Component body) {
        return translateComponent(titleKey).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
                        .withHoverEvent(new HoverEvent.ShowText(translateComponent(hoverTextKey))))
                .append(body);
    }
}
