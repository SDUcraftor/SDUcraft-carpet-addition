package top.sducraft.helpers.litematica;

import carpet.CarpetSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import top.sducraft.config.allItemData.AllItemData;
import top.sducraft.helpers.commands.allItemCommand.SpawnDisplay;
import top.sducraft.helpers.translation.allitem.ItemTranslation;
import java.io.File;
import java.util.*;

import static carpet.utils.Translations.tr;
import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.countItemInWorld;
import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.getCountString;
import static top.sducraft.helpers.commands.allItemCommand.SpawnDisplay.*;

public class MaterialListManenger {
    public static Map<String, Integer> missingMaterials = new HashMap<>();
    public static Map<String, Integer> lackMaterials = new HashMap<>();
//    public static File currentLitematicFile;

    public static Map<String, Integer> getBlockCounts(File litematicFile) {
//        currentLitematicFile = litematicFile;
        Map<String, Integer> blockCounts = new HashMap<>();
        try {
            CompoundTag root = (CompoundTag) new NBTDeserializer(true).fromFile(litematicFile).getTag();
            CompoundTag regions = root.getCompoundTag("Regions");
            for (String regionName : regions.keySet()) {
                CompoundTag region = regions.getCompoundTag(regionName);
                ListTag<CompoundTag> palette = region.getListTag("BlockStatePalette").asCompoundTagList();
                long[] blockStates = region.getLongArray("BlockStates");

                CompoundTag size = region.getCompoundTag("Size");
                int xSize = size.getInt("x");
                int ySize = size.getInt("y");
                int zSize = size.getInt("z");
                int volume = xSize * ySize * zSize;

                BitSet bitSet = BitSet.valueOf(blockStates);
                int paletteBits = Math.max(4, Integer.SIZE - Integer.numberOfLeadingZeros(palette.size() - 1));
                int bitIndex = 0;
                for (int i = 0; i < volume; i++) {
                    int paletteIndex = 0;
                    for (int j = 0; j < paletteBits; j++) {
                        if (bitSet.get(bitIndex + j)) {
                            paletteIndex |= 1 << j;
                        }
                    }
                    bitIndex += paletteBits;
                    if (paletteIndex >= palette.size()) continue;

                    CompoundTag entry = palette.get(paletteIndex);
                    String name = entry.getString("Name");
                    if(name.equals("minecraft:air")) continue;
                    name = name.replaceFirst("wall_", "");
                    name = name.replaceFirst("lava_", "");
                    name = name.replaceFirst("_wire", "");
                    name = name.replace("piston_head", "piston");
                    if (name.startsWith("minecraft:") && name.length() > 10) {
                        name = name.substring(10);
                    }
                    if(CarpetSettings.language.equals("zh_cn")){
                        name = "block.minecraft." + name;
                        name = ItemTranslation.translateItem(name);
                        name = name.replace("block.minecraft.redstone", "红石粉");
                    }
                    blockCounts.put(name, blockCounts.getOrDefault(name, 0) + 1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blockCounts;
    }

    public static void updateMaterial(Map<String, Integer> blockCounts, ServerPlayer player) {
        missingMaterials.clear();
        lackMaterials.clear();

        if (player != null) {
        ServerLevel level = player.getServer().overworld();
        int totalCount = 0;
        int lackCount = 0;
        int missingCount = 0;
            for (Map.Entry<String, Integer> entry : blockCounts.entrySet()) {
                totalCount ++;
                String descriptionId = entry.getKey();
                int requiredCount = entry.getValue();
                AllItemData.ItemData data = AllItemData.search(descriptionId);
                if (data != null) {
                    int availableCount = countItemInWorld(data);
                    if (availableCount < requiredCount) {
                        lackCount ++;
                        for (BlockPos pos : data.chestPos) {
                            spawnBlockDisplay(level, pos, level.getBlockState(pos), 0xFF0000, "material");
                        }
                        lackMaterials.put(descriptionId, requiredCount - availableCount);
                    } else {
                        for (BlockPos pos : data.chestPos) {
                            spawnBlockDisplay(level, pos, level.getBlockState(pos), 0x00FF00, "material");
                        }
                        spawnItemDisplay(data, SpawnDisplay.DisplayType.PERM, "material");
                    }
                } else {
                    missingCount ++;
                    missingMaterials.put(descriptionId, requiredCount);
                }
            }
            player.displayClientMessage(Component.literal(tr("\nTotal material species:"))
                            .append(Component.literal(String.valueOf(totalCount)).withColor(0x00FF00))
                            .append(Component.literal(tr("\nlack material species:")))
                            .append(Component.literal(String.valueOf(lackCount)).withColor(0xFFFF00)).append(Component.literal(tr("\nUse \"/syncmatica material lack\" to display complete list of lack materials")))
                            .append(Component.literal(tr("\nmissing material species:")))
                            .append(Component.literal(String.valueOf(missingCount)).withColor(0xFF0000)).append(Component.literal(tr("\nUse \"/syncmatica material missing\" to display complete list of lack materials")))
            , false);
        }
    }

    public static void listLackMaterial(ServerPlayer player, int page) {
        if (lackMaterials.isEmpty()) {
            player.displayClientMessage(Component.literal(tr("没有缺货的材料。")), false);
            return;
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(lackMaterials.entrySet());
        int ITEMS_PER_PAGE = 10;
        boolean paginate = list.size() > ITEMS_PER_PAGE;
        int totalPages = (list.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, list.size());

        player.displayClientMessage(Component.literal(tr("Lack materials：")), false);

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, Integer> entry = list.get(i);
            int count = entry.getValue();
            player.displayClientMessage(Component.literal("- ").append(entry.getKey()).append(Component.literal(": " + getCountString(count))), false);
        }

        if (paginate) {
            Component pagination;
            if (page == 1 && totalPages > 1) {
                pagination = Component.literal("第 " + page + " 页 / 共 " + totalPages + " 页 ")
                        .append(Component.literal("->").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica material lack " + (page + 1)))
                                .withColor(ChatFormatting.AQUA)));
            } else if (page == totalPages && totalPages > 1) {
                pagination = Component.literal("<-").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica material lack " + (page - 1)))
                                .withColor(ChatFormatting.AQUA))
                        .append(Component.literal(" 第 " + page + " 页 / 共 " + totalPages + " 页"));
            } else {
                pagination = Component.literal("<-").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica material lack " + (page - 1)))
                                .withColor(ChatFormatting.AQUA))
                        .append(Component.literal(" 第 " + page + " 页 / 共 " + totalPages + " 页 "))
                        .append(Component.literal("->").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica material lack " + (page + 1)))
                                .withColor(ChatFormatting.AQUA)));
            }
            player.displayClientMessage(pagination, false);
        }
    }

    public static void listMissingMaterial(ServerPlayer player, int page) {
        if (missingMaterials.isEmpty()) {
            player.displayClientMessage(Component.literal(tr("没有缺失的材料。(不在全物品中)")), false);
            return;
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(missingMaterials.entrySet());
        int ITEMS_PER_PAGE = 10;
        boolean paginate = list.size() > ITEMS_PER_PAGE;
        int totalPages = (list.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, list.size());

        player.displayClientMessage(Component.literal(tr("Missing materials：")), false);

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<String, Integer> entry = list.get(i);
            int count = entry.getValue();
            player.displayClientMessage(Component.literal("- ").append(entry.getKey()).append(Component.literal(": " + getCountString(count))), false);
        }

        if (paginate) {
            Component pagination;
            if (page == 1 && totalPages > 1) {
                pagination = Component.literal("第 " + page + " 页 / 共 " + totalPages + " 页 ")
                        .append(Component.literal("->").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica material missing " + (page + 1)))
                                .withColor(ChatFormatting.AQUA)));
            } else if (page == totalPages && totalPages > 1) {
                pagination = Component.empty().append(Component.literal("<-").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica material missing " + (page - 1)))
                                .withColor(ChatFormatting.AQUA)))
                        .append(Component.literal(" 第 " + page + " 页 / 共 " + totalPages + " 页"));
            } else {
                pagination = Component.empty().append(Component.literal("<-").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica material missing " + (page - 1)))
                                .withColor(ChatFormatting.AQUA)))
                        .append(Component.literal(" 第 " + page + " 页 / 共 " + totalPages + " 页 "))
                        .append(Component.literal("->").withStyle(Style.EMPTY
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/syncmatica material missing " + (page + 1)))
                                .withColor(ChatFormatting.AQUA)));
            }
            player.displayClientMessage(pagination, false);
        }
    }

}
