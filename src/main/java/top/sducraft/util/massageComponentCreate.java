package top.sducraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

public class massageComponentCreate {

    public static Component createDescriptionClickComponent(String label, String url, @Nullable String hoverText, @Nullable String tips) {
        Component description = Component.literal(label)
                .withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                        .withColor(ChatFormatting.GRAY));
        if (hoverText != null) {
            description = Component.empty().append(description).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverText))));
        }
        if (tips != null) {
            description = Component.empty().append(description).append(Component.literal("\n"+tips));
        }
        return description;
    }

    public static Component createCommandClickComponent(String label, String command, @Nullable String hoverText) {
        Component clickcomponent = Component.literal(label)
                .withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                        .withColor(ChatFormatting.AQUA));
        if (hoverText != null) {
            clickcomponent = Component.empty().append(clickcomponent).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverText))));
        }
        return clickcomponent;
    }

    public static Component createSuggestClickComponent(String label, String command, @Nullable String hoverText) {
        Component suggestclickcomponent = Component.literal(label)
                .withStyle(Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                        .withColor(ChatFormatting.AQUA));
        if (hoverText != null) {
            suggestclickcomponent = Component.empty().append(suggestclickcomponent).withStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(hoverText))));
        }
        return suggestclickcomponent;
    }

    public static int getDimensionColor(String dimension) {
            if (dimension.equals("overworld")) {
                return 0x006400;
            }
            else if (dimension.equals("the_nether")) {
                return 0x8B0000;
            }
            else {
                return 0x800080;
            }
    }
}
