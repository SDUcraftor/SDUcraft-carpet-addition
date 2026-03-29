package top.sducraft.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Objects;

import static top.sducraft.util.MassageComponentCreate.createSuggestClickComponent;
import static top.sducraft.util.Message.translateComponent;

public class TickRateManagerEasyCommand extends AbstractDocumentEasyCommand {
    @Override
    public String getCommandName() {
        return "tickratemanager";
    }

    @Override
    protected List<Component> getMessages(ServerPlayer player) {
        Component intro = createDocumentHeader(
                "sducarpet.text.tickrate.intro_title",
                "https://zh.minecraft.wiki/w/%E5%91%BD%E4%BB%A4/tick",
                "sducarpet.text.tickrate.click_doc",
                Component.empty()
                        .append(translateComponent("sducarpet.text.tickrate.intro_body"))
                        .append(translateComponent("sducarpet.text.tickrate.notice_prefix").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))
                                .append(translateComponent(" /leavemessage ").withStyle(Style.EMPTY
                                        .withClickEvent(new ClickEvent.RunCommand("/leavemessage"))
                                        .withColor(ChatFormatting.AQUA)))
                                .append(translateComponent("sducarpet.text.tickrate.message_suffix")).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)))
        );

        ServerTickRateManager tickRateManager = Objects.requireNonNull(player.getServer()).tickRateManager();
        Component tickrate = translateComponent("sducarpet.text.tickrate.current_label")
                .append(Component.literal(String.valueOf(tickRateManager.tickrate()))
                        .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)).append(translateComponent("gt/s")));

        Component actions = Component.empty()
                .append(createSuggestClickComponent("[加速游戏]", "/tick warp", "使用/tick warp <time>来加速指定时间内的游戏速度,默认单位为gt"))
                .append(createSuggestClickComponent("[暂停游戏]", "/tick freeze", "冻结当前游戏"))
                .append(createSuggestClickComponent("[一键恢复正常游戏速度]", "/tick reset", "重置游戏速度"));
        return List.of(intro, tickrate, actions);
    }
}
