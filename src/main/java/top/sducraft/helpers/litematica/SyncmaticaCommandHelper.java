package top.sducraft.helpers.litematica;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static carpet.utils.Translations.tr;

public class SyncmaticaCommandHelper {
    public static void listSyncmatica(ServerPlayer player, int page) {
        List<LoadSyncmatica.Litematica> list = LoadSyncmatica.litematicas;
        if (list.isEmpty()) {
            player.displayClientMessage(Component.literal(("There is no syncmatica")), false);
            return;
        }

        player.displayClientMessage(Component.literal(tr("syncmatica:")), false);

        boolean paginate = list.size() > 10;
        int totalPages = paginate ? (list.size() + 10 - 1) / 10 : 1;

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = paginate ? (page - 1) * 10 : 0;
        int endIndex = paginate ? Math.min(startIndex + 10, list.size()) : list.size();

        for (int i = startIndex; i < endIndex; i++) {
            LoadSyncmatica.Litematica l = list.get(i);
            String owner = l.owner != null ? l.owner.name : "unknown";
            player.displayClientMessage(Component.literal(tr("name: "))
                    .append(Component.literal( l.file_name).withColor(0x00FFFF))
                    .append(Component.literal(tr(" owner: ")))
                    .append(Component.literal(owner).withColor(0xFFFF00)), false);
        }

        if (paginate) {
            Component pagination;
            if (page == 1 && totalPages > 1) {
                pagination = Component.literal("第 " + page + " 页 / 共 " + totalPages + " 页 ")
                        .append(Component.literal("->").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica list " + (page + 1)))
                                .withColor(ChatFormatting.AQUA)));
            } else if (page == totalPages && totalPages > 1) {
                pagination = Component.literal("<-").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica list " + (page - 1)))
                                .withColor(ChatFormatting.AQUA))
                        .append(Component.literal(" 第 " + page + " 页 / 共 " + totalPages + " 页"));
            } else {
                pagination = Component.literal("<-").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica list " + (page - 1)))
                                .withColor(ChatFormatting.AQUA))
                        .append(Component.literal(" 第 " + page + " 页 / 共 " + totalPages + " 页 "))
                        .append(Component.literal("->").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica list " + (page + 1)))
                                .withColor(ChatFormatting.AQUA)));
            }
            player.displayClientMessage(pagination, false);
        }
    }

    public static Set<String> getBlockNames(File litematicFile) {
        Set<String> blockNames = new HashSet<>();
        try {
            CompoundTag root = (CompoundTag) new NBTDeserializer(true).fromFile(litematicFile).getTag();
            CompoundTag regions = root.getCompoundTag("Regions");
            for (String regionName : regions.keySet()) {
                CompoundTag region = regions.getCompoundTag(regionName);
                ListTag<CompoundTag> palette = region.getListTag("BlockStatePalette").asCompoundTagList();
                for (CompoundTag entry : palette) {
                    String name = entry.getString("Name");
                    if (name.startsWith("minecraft:") && name.length() > 10) {
                        name = name.substring(10);
                    }
                    blockNames.add(name);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blockNames;
    }
}
