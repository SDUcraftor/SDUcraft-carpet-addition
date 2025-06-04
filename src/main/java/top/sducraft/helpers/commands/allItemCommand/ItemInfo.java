package top.sducraft.helpers.commands.allItemCommand;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.phys.Vec3;
import top.sducraft.config.allItemData.AllItemData;
import top.sducraft.util.DelayedEvents;

import java.util.*;
import static carpet.utils.Translations.tr;
import static top.sducraft.config.allItemData.AllItemData.dataList;
import static top.sducraft.easyCommand.MachineStatusCommand.getAllItemStatus;

public class ItemInfo {
    public static void displayItemInfo(String name, AllItemData.ItemData data, ServerPlayer player) {
        String typename = Objects.equals(data.type, "item") ? tr("常规物品") : tr("大宗物品");
        Component  title = Component.empty().append(Component.literal("\n"+name).withColor(0xFFFF00)).append(Component.literal(tr("物品信息:\n")));
        int count = countItemInWorld(data);
        String countStr = getCountString(count);
        StringBuilder chestStr = new StringBuilder(" ");
        double ratio = 1 - (double)countRemainCapacity(data)/countCapacity(data);
        for(BlockPos pos : data.chestPos){
            chestStr.append("("+pos.getX()+","+pos.getY()+","+pos.getZ()+")");
        }
        player.displayClientMessage((Component.empty().append(title)
                        .append(Component.literal(tr("分类:")+typename))
                        .append(Component.literal(tr("\n当前储量:"))
                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/allitem info store "+name)))
                        .append(Component.literal(countStr).withColor(0x7EFCFC)
                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/allitem info store "+name))))
                        .append(Component.literal("("+String.format("%.1f%%",ratio * 100)+")").withColor(ratio > 0.8? 0xFF0000 : 0x00FF00))
                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/allitem info store "+name)))
                        .append(Component.literal(tr("\n箱子位置:")+ chestStr)
                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/allitem search "+name))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tr("点击搜索\"")+name+tr("\"物品位置"))))))))
                ,false);
    }

    public static int displayItemStoreInfo(AllItemData.ItemData data, CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.serverLevel();
        Set<BlockPos> allPositions = new HashSet<>();
        if (data.storePos != null) allPositions.addAll(data.storePos);
        if (data.chestPos != null) allPositions.addAll(data.chestPos);

        int count = 0;

        for (BlockPos pos : allPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container) {
                Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
                entity.setBlockState(player.serverLevel().getBlockState(pos));
                entity.setGlowingTag(true);
                entity.setPos(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                entity.addTag("allitem_display");
                level.addFreshEntity(entity);
                DelayedEvents.START_SERVER_TICK.register(200, s -> entity.discard());
                player.lookAt(source.getAnchor(),entity.position().add(0.5, -1.5, 0.5));
                count++;
            }
        }

        player.displayClientMessage(Component.literal(tr("已标记" )+ count + tr(" 个容器位置")), false);
        return count;
    }


    public static int displayAllItemInfo(ServerPlayer player) {
        if (player==null) return 0;
        boolean status = getAllItemStatus();
        player.displayClientMessage(Component.literal(tr("\n全物品信息:\n"))
                .append(Component.literal("状态:")
                        .append(Component.literal(status? tr("运行中"):tr("未运行")).withColor(status ? 0x00FF00 : 0xFF0000))
                ),false);
        displayLackItemInfo(player);
        displayFullItemInfo(player);
        return 1;
    }

    public static void displayLackItemInfo(ServerPlayer player){
        int count = 0;
        for (String key :  dataList.keySet()) {
            AllItemData.ItemData data =dataList.get(key);
            int itemCount = countItemInWorld(data);
            if (itemCount < 100) {
                count++;
            }
        }

        if (count == 0) {
            player.displayClientMessage(Component.literal(tr("无缺货物品")), false);
            return;
        }
        player.displayClientMessage(Component.literal(tr("缺货物品信息:共")+count+tr("个物品缺货,使用"))
                .append(Component.literal("/allitem info lack").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info lack"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tr("点击查看缺货物品详细信息"))))))
                .append(Component.literal("查看详细信息")), false);
    }

    public static void displayLackItemInfoWithPage(ServerPlayer player, int page) {
        final int ITEMS_PER_PAGE = 15;
        final int NO_PAGING_THRESHOLD = 15;
        final int threshold = 100;

        Map<String, AllItemData.ItemData> nameToData = Objects.equals(CarpetSettings.language, "zh_cn") ? AllItemData.chineseNameToData : AllItemData.englishNameToData;
        List<Map.Entry<Component, Integer>> lackItems = new ArrayList<>();
        for (Map.Entry<String, AllItemData.ItemData> entry : nameToData.entrySet()) {
            String key = entry.getKey();
            AllItemData.ItemData data = entry.getValue();
            int count = countItemInWorld(data);
            if (count < threshold) {
                String typename = Objects.equals(data.type, "item") ? tr("(常规物品)") : tr("(大宗物品)");
                lackItems.add(Map.entry(Component.empty()
                        .append(Component.literal(typename))
                        .append(Component.literal(key).withColor(0xFFFF00))
                        .append(getCountString(count))
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info "+key))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tr("点击查看")+key+(tr("详细信息")))))), count));
            }
        }

        if (lackItems.isEmpty()) {
            player.displayClientMessage(Component.literal(tr("\n无缺货物品")), false);
            return;
        }
        player.displayClientMessage(Component.literal(tr("\n缺货物品信息:")), false);
        lackItems.sort(Comparator.comparingInt(Map.Entry::getValue));

        boolean paginate = lackItems.size() > NO_PAGING_THRESHOLD;
        int totalPages = paginate ? (lackItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE : 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        int startIndex = paginate ? (page - 1) * ITEMS_PER_PAGE : 0;
        int endIndex = paginate ? Math.min(startIndex + ITEMS_PER_PAGE, lackItems.size()) : lackItems.size();

        for (int i = startIndex; i < endIndex; i++) {
            Map.Entry<Component, Integer> entry = lackItems.get(i);
            player.displayClientMessage(entry.getKey(), false);
        }

        if (paginate) {
            if(page < totalPages && page > 1) {
                player.displayClientMessage(Component.empty().append(Component.literal("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info lack " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages)).append(Component.literal(" 页")))
                                .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info lack " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            }
            else if (page == 1) {
                player.displayClientMessage(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages)).append(Component.literal(" 页"))
                                .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info lack " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            }
            else {
                player.displayClientMessage(Component.empty().append(Component.literal("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info lack " + (page - 1)))))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages)).append(Component.literal(" 页")))), false);
            }
        }
    }

    public static void displayFullItemInfo(ServerPlayer player){
        Map<String, AllItemData.ItemData> nameToData =
                Objects.equals(CarpetSettings.language, "zh_cn") ? AllItemData.chineseNameToData : AllItemData.englishNameToData;
        int count = 0;
        for (String key :  dataList.keySet()) {
            AllItemData.ItemData data =dataList.get(key);
            double ratio = 1- (double) countRemainCapacity(data) /countCapacity(data);
            if (ratio>=0.8) {
                count++;
            }
        }

        if (count == 0) {
            player.displayClientMessage(Component.literal(tr("没有即将爆仓物品")), false);
            return;
        }

        player.displayClientMessage(Component.literal(tr("即将爆仓物品信息:共")+count+tr("个物品即将爆仓,使用"))
                .append(Component.literal("/allitem info full").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info full"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tr("点击查看即将爆仓物品详细信息"))))))
                .append(Component.literal(tr("查看详细信息"))), false);
    }

    public static void displayFullItemInfoWithPage(ServerPlayer player, int page) {
        final int ITEMS_PER_PAGE = 15;
        final int NO_PAGING_THRESHOLD = 15;

        Map<String, AllItemData.ItemData> nameToData =
                Objects.equals(CarpetSettings.language, "zh_cn") ? AllItemData.chineseNameToData : AllItemData.englishNameToData;

        List<Map.Entry<Component, Double>> fullItems = new ArrayList<>();

        for (Map.Entry<String, AllItemData.ItemData> entry : nameToData.entrySet()) {
            String key = entry.getKey();
            AllItemData.ItemData data = entry.getValue();

            int current = countRemainCapacity(data);
            int total = countCapacity(data);
            if (total == 0) continue;
            double ratio = 1- (double) current / total;

            if (ratio >= 0.8) {
                String typename = Objects.equals(data.type, "item") ? tr("(常规物品)") : tr("(大宗物品)");
                String percent = String.format("%.1f%%", ratio * 100);
                Component line = Component.empty()
                        .append(Component.literal(typename))
                        .append(Component.literal(key).withColor(0xFFFF00))
                        .append(Component.literal(" (" + percent + ") ").withColor(0xFF0000))
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info "+key))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tr("点击查看")+key+(tr("详细信息"))))));
                fullItems.add(Map.entry(line, ratio));
            }
        }

        if (fullItems.isEmpty()) {
            player.displayClientMessage(Component.literal(tr("\n没有即将爆仓物品")), false);
            return;
        }

        player.displayClientMessage(Component.literal(tr("\n即将爆仓物品信息:")), false);

        fullItems.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        boolean paginate = fullItems.size() > NO_PAGING_THRESHOLD;
        int totalPages = paginate ? (fullItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE : 1;
        page = Math.max(1, Math.min(page, totalPages));

        int startIndex = paginate ? (page - 1) * ITEMS_PER_PAGE : 0;
        int endIndex = paginate ? Math.min(startIndex + ITEMS_PER_PAGE, fullItems.size()) : fullItems.size();

        for (int i = startIndex; i < endIndex; i++) {
            player.displayClientMessage(fullItems.get(i).getKey(), false);
        }

        if (paginate) {
            if (page > 1 && page < totalPages) {
                player.displayClientMessage(Component.empty()
                        .append(Component.literal("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info full " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages)).append(Component.literal(" 页")))
                                .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info full " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else if (page == 1) {
                player.displayClientMessage(Component.literal(tr("第 "))
                        .append(Component.literal(String.valueOf(page)))
                        .append(Component.literal(tr(" 页 / 共 ")))
                        .append(Component.literal(String.valueOf(totalPages)))
                        .append(Component.literal(" 页"))
                        .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info full " + (page + 1))).withColor(ChatFormatting.AQUA))), false);
            } else {
                player.displayClientMessage(Component.empty()
                        .append(Component.literal("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info full " + (page - 1)))))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages))).append(Component.literal(" 页"))), false);
            }
        }
    }

    public static void displayAllItemInfoWithPage(ServerPlayer player, int page) {
        final int ITEMS_PER_PAGE = 15;
        final int NO_PAGING_THRESHOLD = 15;
        Map<String, AllItemData.ItemData> nameToData =
                Objects.equals(CarpetSettings.language, "zh_cn") ? AllItemData.chineseNameToData : AllItemData.englishNameToData;
        List<Map.Entry<Component, Double>> fullItems = new ArrayList<>();
        for (Map.Entry<String, AllItemData.ItemData> entry : nameToData.entrySet()) {
            String key = entry.getKey();
            AllItemData.ItemData data = entry.getValue();
            int current = countRemainCapacity(data);
            int total = countCapacity(data);
            if (total == 0) continue;
            double ratio = 1- (double) current / total;
                String typename = Objects.equals(data.type, "item") ? tr("(常规物品)") : tr("(大宗物品)");
                String percent = String.format("%.1f%%", ratio * 100);
                Component line = Component.empty()
                        .append(Component.literal(typename))
                        .append(Component.literal(key).withColor(0xFFFF00))
                        .append(Component.literal(" ("))
                        .append(Component.literal(percent).withColor(ratio > 0.8? 0xFF0000 : 0x00FF00))
                        .append(Component.literal(")"))
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info "+key))
                                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tr("点击查看")+key+(tr("详细信息"))))));
                fullItems.add(Map.entry(line, ratio));
        }

        player.displayClientMessage(Component.literal(tr("\n物品信息:")), false);
        fullItems.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        boolean paginate = fullItems.size() > NO_PAGING_THRESHOLD;
        int totalPages = paginate ? (fullItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE : 1;
        page = Math.max(1, Math.min(page, totalPages));

        int startIndex = paginate ? (page - 1) * ITEMS_PER_PAGE : 0;
        int endIndex = paginate ? Math.min(startIndex + ITEMS_PER_PAGE, fullItems.size()) : fullItems.size();

        for (int i = startIndex; i < endIndex; i++) {
            player.displayClientMessage(fullItems.get(i).getKey(), false);
        }

        if (paginate) {
            if (page > 1 && page < totalPages) {
                player.displayClientMessage(Component.empty()
                        .append(Component.literal("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info all " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages)).append(Component.literal(" 页")))
                                .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info all " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else if (page == 1) {
                player.displayClientMessage(Component.literal(tr("第 "))
                        .append(Component.literal(String.valueOf(page)))
                        .append(Component.literal(tr(" 页 / 共 ")))
                        .append(Component.literal(String.valueOf(totalPages)))
                        .append(Component.literal(" 页"))
                        .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info all " + (page + 1))).withColor(ChatFormatting.AQUA))), false);
            } else {
                player.displayClientMessage(Component.empty()
                        .append(Component.literal("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info all " + (page - 1)))))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages))).append(Component.literal(" 页"))), false);
            }
        }
    }

    public static int displayItemInfoWithPage(ServerPlayer player, int page ,String type) {
        final int ITEMS_PER_PAGE = 15;
        final int NO_PAGING_THRESHOLD = 15;
        Map<String, AllItemData.ItemData> nameToData =
                Objects.equals(CarpetSettings.language, "zh_cn") ? AllItemData.chineseNameToData : AllItemData.englishNameToData;
        List<Map.Entry<Component, Double>> fullItems = new ArrayList<>();
        for (Map.Entry<String, AllItemData.ItemData> entry : nameToData.entrySet()) {
            String key = entry.getKey();
            AllItemData.ItemData data = entry.getValue();
            if(type.equals(data.type)){
            int current = countRemainCapacity(data);
            int total = countCapacity(data);
            if (total == 0) continue;
            double ratio = 1- (double) current / total;
            String typename = Objects.equals(data.type, "item") ? tr("(常规物品)") : tr("(大宗物品)");
            String percent = String.format("%.1f%%", ratio * 100);
            Component line = Component.empty()
                    .append(Component.literal(typename))
                    .append(Component.literal(key).withColor(0xFFFF00))
                    .append(Component.literal(" ("))
                    .append(Component.literal(percent).withColor(ratio > 0.8? 0xFF0000 : 0x00FF00))
                    .append(Component.literal(")"))
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info "+key))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(tr("点击查看")+key+(tr("详细信息"))))));
            fullItems.add(Map.entry(line, ratio));
            }
        }

        player.displayClientMessage(Component.literal(type.equals("bulk")?tr("大宗物品信息:"):tr("常规物品信息:")), false);
        fullItems.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        boolean paginate = fullItems.size() > NO_PAGING_THRESHOLD;
        int totalPages = paginate ? (fullItems.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE : 1;
        page = Math.max(1, Math.min(page, totalPages));

        int startIndex = paginate ? (page - 1) * ITEMS_PER_PAGE : 0;
        int endIndex = paginate ? Math.min(startIndex + ITEMS_PER_PAGE, fullItems.size()) : fullItems.size();

        for (int i = startIndex; i < endIndex; i++) {
            player.displayClientMessage(fullItems.get(i).getKey(), false);
        }

        if (paginate) {
            if (page > 1 && page < totalPages) {
                player.displayClientMessage(Component.empty()
                        .append(Component.literal("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info " + (type.equals("bulk")?"bulk":"custom") + " " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages)).append(Component.literal(" 页")))
                                .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info " + (type.equals("bulk")?"bulk":"custom") + " " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else if (page == 1) {
                player.displayClientMessage(Component.literal(tr("第 "))
                        .append(Component.literal(String.valueOf(page)))
                        .append(Component.literal(tr(" 页 / 共 ")))
                        .append(Component.literal(String.valueOf(totalPages)))
                        .append(Component.literal(" 页"))
                        .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info " + (type.equals("bulk")?"bulk":"custom") + " " + (page + 1))).withColor(ChatFormatting.AQUA))), false);
            } else {
                player.displayClientMessage(Component.empty()
                        .append(Component.literal("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/allitem info " + (type.equals("bulk")?"bulk":"custom") + " " + (page - 1)))))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages))).append(Component.literal(" 页"))), false);
            }
        }
        return 1;
    }


    public static int countItemInWorld(AllItemData.ItemData data) {
        ServerLevel level = CarpetServer.minecraft_server.overworld();
        Set<BlockPos> positions = data.storePos;
        positions.addAll(data.chestPos);
        return countItemInContainers(level,positions);
    }

    private static int countItemInContainers(ServerLevel level,Set<BlockPos> positions) {
        int count = 0;
        for (BlockPos pos : positions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (!stack.isEmpty()) {
                        if (stack.getItem().getDescriptionId().contains("shulker_box")) {
                            count += countItemsInShulkerBox(stack);
                        }
                        else {
                            count += stack.getCount();
                        }
                    }
                }
            }
        }
        return count;
      }
    private static int countItemsInShulkerBox(ItemStack stack) {
        int count = 0;
        CompoundTag tag =stack.getTag();
        if (tag != null && tag.contains("BlockEntityTag")) {
            CompoundTag bet = tag.getCompound("BlockEntityTag");
            if (bet.contains("Items", 9)) {
                ListTag items = bet.getList("Items", 10);
                for (int j = 0; j < items.size(); j++) {
                    CompoundTag itemTag = items.getCompound(j);
                    count += itemTag.getByte("Count");
                }
            }
        }
        return count;
    }

    public static int countRemainCapacity(AllItemData.ItemData data) {
        int remainCapacity = 0;
        Set<BlockPos> allPositions = new HashSet<>();
        if (data.storePos != null) allPositions.addAll(data.storePos);
        if (data.chestPos != null) allPositions.addAll(data.chestPos);

        for (BlockPos pos : allPositions) {
            BlockEntity be = CarpetServer.minecraft_server.overworld().getBlockEntity(pos);
            if (!(be instanceof Container container)) continue;
            if ("bulk".equals(data.type)) {
                if (be instanceof ShulkerBoxBlockEntity) {
                    int total = container.getContainerSize() * 64;
                    int used = 0;
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        ItemStack stack = container.getItem(i);
                        used += stack.getCount();
                    }
                    remainCapacity += Math.max(0, total - used);
                } else {
                    for (int i = 0; i < container.getContainerSize(); i++) {
                        if (container.getItem(i).isEmpty()) {
                            remainCapacity += 1728;
                        }
                    }
                }
            } else {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (stack.isEmpty()) {
                        remainCapacity += 64;
                    } else if (stack.getCount() < stack.getMaxStackSize()) {
                        remainCapacity += stack.getMaxStackSize() - stack.getCount();
                    }
                }
            }
        }

        return remainCapacity;
    }

    public static int countCapacity(AllItemData.ItemData data) {
        int totalSlots = 0;
        int totalCount = 0;

        Set<BlockPos> allPositions = new HashSet<>();
        if (data.storePos != null) allPositions.addAll(data.storePos);
        if (data.chestPos != null) allPositions.addAll(data.chestPos);

        for (BlockPos pos : allPositions) {

            BlockEntity be = CarpetServer.minecraft_server.overworld().getBlockEntity(pos);
            if ( be instanceof ShulkerBoxBlockEntity ) {
                totalCount += 1728;
            }
            else if(be instanceof Container container){
                totalSlots += container.getContainerSize();
            }
        }
        int perSlotCapacity = data.type.equals("bulk") ? 1728 : 64;
        totalCount  += totalSlots * perSlotCapacity;
        return totalCount;
    }

    public static String getCountString(int count){
        String countStr;
        if (count > 100000) {
            countStr = String.format(Locale.ROOT, "%.2e", (double) count);
            String shulkcount = count/1728 + tr("盒")+ count % 1728/64 + tr("组")+ count%64 + tr("个");
            countStr = countStr +"(" + shulkcount + ")";
        } else if (count > 5000) {
            countStr = String.valueOf(count);
            String shulkcount = count/1728 + tr("盒")+ count % 1728/64 + tr("组")+ count%64 + tr("个");
            countStr = countStr +"(" + shulkcount + ")";
        }
        else {
            countStr = String.valueOf(count);
        }
        return countStr;
    }
}
