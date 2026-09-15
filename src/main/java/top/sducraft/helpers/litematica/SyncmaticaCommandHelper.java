package top.sducraft.helpers.litematica;

import carpet.CarpetServer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static top.sducraft.util.Message.translateComponent;

public class SyncmaticaCommandHelper {

    public static void listSyncmatica(ServerPlayer player, int page) {
        List<LoadSyncmatica.Litematica> list = LoadSyncmatica.litematicas;
        if (list.isEmpty()) {
            player.sendSystemMessage(translateComponent("sducarpet.syncmatica.none"));
            return;
        }

        player.sendSystemMessage(translateComponent("sducarpet.syncmatica.title"));

        boolean paginate = list.size() > 10;
        int totalPages = paginate ? (list.size() + 10 - 1) / 10 : 1;

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = paginate ? (page - 1) * 10 : 0;
        int endIndex = paginate ? Math.min(startIndex + 10, list.size()) : list.size();

        for (int i = startIndex; i < endIndex; i++) {
            LoadSyncmatica.Litematica l = list.get(i);
            String owner = l.owner != null ? l.owner.name : "unknown";
            player.sendSystemMessage(translateComponent("sducarpet.syncmatica.name")
                    .append(Component.literal(l.file_name).withColor(0x00FFFF))
                    .append(translateComponent("sducarpet.syncmatica.owner"))
                    .append(Component.literal(owner).withColor(0xFFFF00)));
        }

        if (paginate) {
            Component pagination;
            if (page == 1 && totalPages > 1) {
                pagination = translateComponent("sducarpet.pagination.pagePrefix")
                        .append(String.valueOf(page))
                        .append(translateComponent("sducarpet.pagination.pageMiddle"))
                        .append(String.valueOf(totalPages))
                        .append(translateComponent("sducarpet.pagination.pageSuffixWithSpace"))
                        .append(translateComponent("->").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent.RunCommand("/syncmatica list " + (page + 1)))
                                .withColor(ChatFormatting.AQUA)));
            } else if (page == totalPages && totalPages > 1) {
                pagination = translateComponent("<-").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent.RunCommand("/syncmatica list " + (page - 1)))
                                .withColor(ChatFormatting.AQUA))
                        .append(translateComponent("sducarpet.pagination.pagePrefixWithLeadingSpace"))
                        .append(String.valueOf(page))
                        .append(translateComponent("sducarpet.pagination.pageMiddle"))
                        .append(String.valueOf(totalPages))
                        .append(translateComponent("sducarpet.pagination.pageSuffix"));
            } else {
                pagination = translateComponent("<-").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent.RunCommand("/syncmatica list " + (page - 1)))
                                .withColor(ChatFormatting.AQUA))
                        .append(translateComponent("sducarpet.pagination.pagePrefixWithLeadingSpace"))
                        .append(String.valueOf(page))
                        .append(translateComponent("sducarpet.pagination.pageMiddle"))
                        .append(String.valueOf(totalPages))
                        .append(translateComponent("sducarpet.pagination.pageSuffixWithSpace"))
                        .append(translateComponent("->").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent.RunCommand("/syncmatica list " + (page + 1)))
                                .withColor(ChatFormatting.AQUA)));
            }
            player.sendSystemMessage(pagination);
        }
    }


    public static void tt() {
        for (ServerLevel serverLevel : CarpetServer.minecraft_server.getAllLevels()) {

        }

    }

}
