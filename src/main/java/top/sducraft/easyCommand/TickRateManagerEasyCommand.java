package top.sducraft.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.Message.translateComponent;
import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;

public class TickRateManagerEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "tickratemanager";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[游戏速度控制]", "/easycommand tickratemanager", "点击进入游戏界面控制界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) throws URISyntaxException {
        Component component = translateComponent("sducarpet.text.tickrate.intro_title").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent.OpenUrl(new URI("https://zh.minecraft.wiki/w/%E5%91%BD%E4%BB%A4/tick")))
                        .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.text.tickrate.click_doc"))))
                .append(translateComponent("sducarpet.text.tickrate.intro_body"))
                .append(translateComponent("sducarpet.text.tickrate.notice_prefix").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
                        .append(translateComponent(" /leavemessage ").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent.RunCommand("/leavemessage"))
                                .withColor(ChatFormatting.AQUA)))
                        .append(translateComponent("sducarpet.text.tickrate.message_suffix")).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));

        ServerTickRateManager tickRateManager = Objects.requireNonNull(player.getServer()).tickRateManager();
        Component tickrate = translateComponent("sducarpet.text.tickrate.current_label")
                .append(Component.literal(String.valueOf(tickRateManager.tickrate()))
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)).append(translateComponent("gt/s")));

        player.displayClientMessage(component, false);
        player.displayClientMessage(tickrate, false);
        player.displayClientMessage(Component.empty()
                .append(createSuggestClickComponent("[加速游戏]", "/tick warp", "使用/tick warp <time>来加速指定时间内的游戏速度,默认单位为gt"))
                .append(createSuggestClickComponent("[暂停游戏]", "/tick freeze", "冻结当前游戏"))
                .append(createSuggestClickComponent("[一键恢复正常游戏速度]", "/tick reset", "重置游戏速度")), false);
    }
}
