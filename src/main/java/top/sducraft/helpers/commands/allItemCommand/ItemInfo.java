package top.sducraft.helpers.commands.allItemCommand;

import carpet.CarpetServer;
import carpet.CarpetSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
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
import static top.sducraft.util.Message.translateComponent;
import static top.sducraft.config.allItemData.AllItemData.dataList;
import static top.sducraft.easyCommand.MachineStatusEasyCommand.getAllItemStatus;

public class ItemInfo {
    public static void displayItemInfo(String name, AllItemData.ItemData data, ServerPlayer player) {
        String typename = Objects.equals(data.type, "item") ? translateComponent("sducarpet.text.allitem.type_normal").getString() : translateComponent("sducarpet.text.allitem.type_bulk").getString();
        Component title = Component.empty().append(Component.literal("\n" + name).withColor(0xFFFF00)).append(translateComponent("sducarpet.text.allitem.item_info_title"));
        int count = countItemInWorld(data);
        String countStr = getCountString(count);
        StringBuilder chestStr = new StringBuilder(" ");
        double ratio = 1 - (double) countRemainCapacity(data) / countCapacity(data);
        for (BlockPos pos : data.chestPos) {
            chestStr.append("(").append(pos.getX()).append(",").append(pos.getY()).append(",").append(pos.getZ()).append(")");
        }
        player.displayClientMessage((Component.empty().append(title)
                        .append(translateComponent("sducarpet.text.allitem.category_label").append(typename))
                        .append(translateComponent("sducarpet.text.allitem.current_stock_label")
                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info store " + name)))
                                .append(Component.literal(countStr).withColor(0x7EFCFC)
                                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info store " + name))))
                                .append(Component.literal("(" + String.format("%.1f%%", ratio * 100) + ")").withColor(ratio > 0.8 ? 0xFF0000 : 0x00FF00))
                                .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info store " + name)))
                                .append(translateComponent("sducarpet.text.allitem.chest_positions_label").append(chestStr.toString())
                                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem search " + name))
                                                .withHoverEvent(new HoverEvent.ShowText(translateComponent("点击搜索\"").append(name).append(translateComponent("\"物品位置"))))))))
                , false);
    }

    public static int displayItemStoreInfo(AllItemData.ItemData data, CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        ServerLevel level = player.level();
        Set<BlockPos> allPositions = new HashSet<>();
        if (data.storePos != null) allPositions.addAll(data.storePos);
        if (data.chestPos != null) allPositions.addAll(data.chestPos);

        int count = 0;

        for (BlockPos pos : allPositions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container) {
                Display.BlockDisplay entity = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
                entity.setBlockState(player.level().getBlockState(pos));
                entity.setGlowingTag(true);
                entity.setPos(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
                entity.addTag("allitem_display");
                level.addFreshEntity(entity);
                DelayedEvents.START_SERVER_TICK.register(200, s -> entity.discard());
                player.lookAt(source.getAnchor(), entity.position().add(0.5, -1.5, 0.5));
                count++;
            }
        }

        player.displayClientMessage(translateComponent("sducarpet.text.allitem.marked_prefix").append(String.valueOf(count)).append(translateComponent("sducarpet.text.allitem.container_positions_suffix")), false);
        return count;
    }


    public static int displayAllItemInfo(ServerPlayer player) {
        if (player == null) return 0;
        boolean status = getAllItemStatus();
        player.displayClientMessage(translateComponent("sducarpet.text.allitem.overview_title")
                .append(translateComponent("sducarpet.text.common.status_label")
                        .append(Component.literal(status ? translateComponent("sducarpet.text.allitem.running").getString() : translateComponent("sducarpet.text.allitem.not_running").getString()).withColor(status ? 0x00FF00 : 0xFF0000))
                ), false);
        displayLackItemInfo(player);
        displayFullItemInfo(player);
        return 1;
    }

    public static void displayLackItemInfo(ServerPlayer player) {
        int count = 0;
        for (String key : dataList.keySet()) {
            AllItemData.ItemData data = dataList.get(key);
            int itemCount = countItemInWorld(data);
            if (itemCount < 100) {
                count++;
            }
        }

        if (count == 0) {
            player.displayClientMessage(translateComponent("sducarpet.text.allitem.lack_none"), false);
            return;
        }
        player.displayClientMessage(translateComponent("sducarpet.text.allitem.lack_info_prefix").append(String.valueOf(count)).append(translateComponent("sducarpet.text.allitem.lack_info_suffix"))
                .append(translateComponent("/allitem info lack").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent.RunCommand("/allitem info lack"))
                        .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.text.allitem.click_lack_details")))))
                .append(translateComponent("sducarpet.text.common.view_details")), false);
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
                String typename = Objects.equals(data.type, "item") ? translateComponent("sducarpet.text.allitem.type_normal_bracket").getString() : translateComponent("sducarpet.text.allitem.type_bulk_bracket").getString();
                lackItems.add(Map.entry(Component.empty()
                        .append(Component.literal(typename))
                        .append(Component.literal(key).withColor(0xFFFF00))
                        .append(getCountString(count))
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info " + key))
                                .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.text.common.click_view").append(key).append(translateComponent("sducarpet.text.common.details_suffix"))))), count));
            }
        }

        if (lackItems.isEmpty()) {
            player.displayClientMessage(translateComponent("sducarpet.text.allitem.lack_none_with_newline"), false);
            return;
        }
        player.displayClientMessage(translateComponent("sducarpet.text.allitem.lack_list_title"), false);
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
            if (page < totalPages && page > 1) {
                player.displayClientMessage(Component.empty().append(translateComponent("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info lack " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                                .append(Component.literal(String.valueOf(totalPages)).append(translateComponent("sducarpet.text.pagination.page_suffix")))
                                .append(translateComponent("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info lack " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else if (page == 1) {
                player.displayClientMessage(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                        .append(Component.literal(String.valueOf(totalPages)).append(translateComponent("sducarpet.text.pagination.page_suffix"))
                                .append(translateComponent("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info lack " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else {
                player.displayClientMessage(Component.empty().append(translateComponent("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info lack " + (page - 1)))))
                        .append(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                                .append(Component.literal(String.valueOf(totalPages)).append(translateComponent("sducarpet.text.pagination.page_suffix")))), false);
            }
        }
    }

    public static void displayFullItemInfo(ServerPlayer player) {
        Map<String, AllItemData.ItemData> nameToData =
                Objects.equals(CarpetSettings.language, "zh_cn") ? AllItemData.chineseNameToData : AllItemData.englishNameToData;
        int count = 0;
        for (String key : dataList.keySet()) {
            AllItemData.ItemData data = dataList.get(key);
            double ratio = 1 - (double) countRemainCapacity(data) / countCapacity(data);
            if (ratio >= 0.8) {
                count++;
            }
        }

        if (count == 0) {
            player.displayClientMessage(translateComponent("sducarpet.text.allitem.full_none"), false);
            return;
        }

        player.displayClientMessage(translateComponent("sducarpet.text.allitem.full_info_prefix").append(String.valueOf(count)).append(translateComponent("sducarpet.text.allitem.full_info_suffix"))
                .append(translateComponent("/allitem info full").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent.RunCommand("/allitem info full"))
                        .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.text.allitem.click_full_details")))))
                .append(translateComponent("sducarpet.text.common.view_details")), false);
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
            double ratio = 1 - (double) current / total;

            if (ratio >= 0.8) {
                String typename = Objects.equals(data.type, "item") ? translateComponent("sducarpet.text.allitem.type_normal_bracket").getString() : translateComponent("sducarpet.text.allitem.type_bulk_bracket").getString();
                String percent = String.format("%.1f%%", ratio * 100);
                Component line = Component.empty()
                        .append(Component.literal(typename))
                        .append(Component.literal(key).withColor(0xFFFF00))
                        .append(Component.literal(" (" + percent + ") ").withColor(0xFF0000))
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info " + key))
                                .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.text.common.click_view").append(key).append(translateComponent("sducarpet.text.common.details_suffix")))));
                fullItems.add(Map.entry(line, ratio));
            }
        }

        if (fullItems.isEmpty()) {
            player.displayClientMessage(translateComponent("sducarpet.text.allitem.full_none_with_newline"), false);
            return;
        }

        player.displayClientMessage(translateComponent("sducarpet.text.allitem.full_list_title"), false);

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
                        .append(translateComponent("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info full " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                                .append(Component.literal(String.valueOf(totalPages)).append(translateComponent("sducarpet.text.pagination.page_suffix")))
                                .append(translateComponent("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info full " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else if (page == 1) {
                player.displayClientMessage(translateComponent("sducarpet.text.pagination.prefix")
                        .append(Component.literal(String.valueOf(page)))
                        .append(translateComponent("sducarpet.text.pagination.middle"))
                        .append(Component.literal(String.valueOf(totalPages)))
                        .append(translateComponent("sducarpet.text.pagination.page_suffix"))
                        .append(translateComponent("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info full " + (page + 1))).withColor(ChatFormatting.AQUA))), false);
            } else {
                player.displayClientMessage(Component.empty()
                        .append(translateComponent("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info full " + (page - 1)))))
                        .append(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                                .append(Component.literal(String.valueOf(totalPages))).append(translateComponent("sducarpet.text.pagination.page_suffix"))), false);
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
            double ratio = 1 - (double) current / total;
            String typename = Objects.equals(data.type, "item") ? translateComponent("sducarpet.text.allitem.type_normal_bracket").getString() : translateComponent("sducarpet.text.allitem.type_bulk_bracket").getString();
            String percent = String.format("%.1f%%", ratio * 100);
            Component line = Component.empty()
                    .append(Component.literal(typename))
                    .append(Component.literal(key).withColor(0xFFFF00))
                    .append(translateComponent(" ("))
                    .append(Component.literal(percent).withColor(ratio > 0.8 ? 0xFF0000 : 0x00FF00))
                    .append(translateComponent(")"))
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info " + key))
                            .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.text.common.click_view").append(key).append(translateComponent("sducarpet.text.common.details_suffix")))));
            fullItems.add(Map.entry(line, ratio));
        }

        player.displayClientMessage(translateComponent("sducarpet.text.allitem.item_list_title"), false);
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
                        .append(translateComponent("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info all " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                                .append(Component.literal(String.valueOf(totalPages)).append(translateComponent("sducarpet.text.pagination.page_suffix")))
                                .append(translateComponent("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info all " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else if (page == 1) {
                player.displayClientMessage(translateComponent("sducarpet.text.pagination.prefix")
                        .append(Component.literal(String.valueOf(page)))
                        .append(translateComponent("sducarpet.text.pagination.middle"))
                        .append(Component.literal(String.valueOf(totalPages)))
                        .append(translateComponent("sducarpet.text.pagination.page_suffix"))
                        .append(translateComponent("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info all " + (page + 1))).withColor(ChatFormatting.AQUA))), false);
            } else {
                player.displayClientMessage(Component.empty()
                        .append(translateComponent("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info all " + (page - 1)))))
                        .append(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                                .append(Component.literal(String.valueOf(totalPages))).append(translateComponent("sducarpet.text.pagination.page_suffix"))), false);
            }
        }
    }

    public static int displayItemInfoWithPage(ServerPlayer player, int page, String type) {
        final int ITEMS_PER_PAGE = 15;
        final int NO_PAGING_THRESHOLD = 15;
        Map<String, AllItemData.ItemData> nameToData =
                Objects.equals(CarpetSettings.language, "zh_cn") ? AllItemData.chineseNameToData : AllItemData.englishNameToData;
        List<Map.Entry<Component, Double>> fullItems = new ArrayList<>();
        for (Map.Entry<String, AllItemData.ItemData> entry : nameToData.entrySet()) {
            String key = entry.getKey();
            AllItemData.ItemData data = entry.getValue();
            if (type.equals(data.type)) {
                int current = countRemainCapacity(data);
                int total = countCapacity(data);
                if (total == 0) continue;
                double ratio = 1 - (double) current / total;
                String typename = Objects.equals(data.type, "item") ? translateComponent("sducarpet.text.allitem.type_normal_bracket").getString() : translateComponent("sducarpet.text.allitem.type_bulk_bracket").getString();
                String percent = String.format("%.1f%%", ratio * 100);
                Component line = Component.empty()
                        .append(Component.literal(typename))
                        .append(Component.literal(key).withColor(0xFFFF00))
                        .append(translateComponent(" ("))
                        .append(Component.literal(percent).withColor(ratio > 0.8 ? 0xFF0000 : 0x00FF00))
                        .append(translateComponent(")"))
                        .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info " + key))
                                .withHoverEvent(new HoverEvent.ShowText(translateComponent("sducarpet.text.common.click_view").append(key).append(translateComponent("sducarpet.text.common.details_suffix")))));
                fullItems.add(Map.entry(line, ratio));
            }
        }

        player.displayClientMessage(translateComponent(type.equals("bulk") ? "大宗物品信息:" : "常规物品信息:"), false);
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
                        .append(translateComponent("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info " + (type.equals("bulk") ? "bulk" : "custom") + " " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                                .append(Component.literal(String.valueOf(totalPages)).append(translateComponent("sducarpet.text.pagination.page_suffix")))
                                .append(translateComponent("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info " + (type.equals("bulk") ? "bulk" : "custom") + " " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else if (page == 1) {
                player.displayClientMessage(translateComponent("sducarpet.text.pagination.prefix")
                        .append(Component.literal(String.valueOf(page)))
                        .append(translateComponent("sducarpet.text.pagination.middle"))
                        .append(Component.literal(String.valueOf(totalPages)))
                        .append(translateComponent("sducarpet.text.pagination.page_suffix"))
                        .append(translateComponent("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info " + (type.equals("bulk") ? "bulk" : "custom") + " " + (page + 1))).withColor(ChatFormatting.AQUA))), false);
            } else {
                player.displayClientMessage(Component.empty()
                        .append(translateComponent("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/allitem info " + (type.equals("bulk") ? "bulk" : "custom") + " " + (page - 1)))))
                        .append(translateComponent("sducarpet.text.pagination.prefix").append(Component.literal(String.valueOf(page))).append(translateComponent("sducarpet.text.pagination.middle"))
                                .append(Component.literal(String.valueOf(totalPages))).append(translateComponent("sducarpet.text.pagination.page_suffix"))), false);
            }
        }
        return 1;
    }


    public static int countItemInWorld(AllItemData.ItemData data) {
        ServerLevel level = CarpetServer.minecraft_server.overworld();
        Set<BlockPos> positions = data.storePos;
        positions.addAll(data.chestPos);
        return countItemInContainers(level, positions);
    }

    private static int countItemInContainers(ServerLevel level, Set<BlockPos> positions) {
        int count = 0;
        for (BlockPos pos : positions) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof Container container) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    ItemStack stack = container.getItem(i);
                    if (!stack.isEmpty()) {
                        if (stack.getDisplayName().getString().contains("shulker_box")) {
//                            count += countItemsInShulkerBox(stack);
                        } else {
                            count += stack.getCount();
                        }
                    }
                }
            }
        }
        return count;
    }
//    private static int countItemsInShulkerBox(ItemStack stack) {
//        int count = 0;
//        CustomData bet = stack.get(DataComponents.BLOCK_ENTITY_DATA);
//        if (bet != null && bet.contains("Items", 9)) {
//            ListTag items = bet.getList("Items", 10); // tag type 10 = CompoundTag
//            for (int j = 0; j < items.size(); j++) {
//                CompoundTag itemTag = items.getCompound(j);
//                count += itemTag.getByte("Count");
//            }
//        }
//        return count;
//    }

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
            if (be instanceof ShulkerBoxBlockEntity) {
                totalCount += 1728;
            } else if (be instanceof Container container) {
                totalSlots += container.getContainerSize();
            }
        }
        int perSlotCapacity = data.type.equals("bulk") ? 1728 : 64;
        totalCount += totalSlots * perSlotCapacity;
        return totalCount;
    }

    public static String getCountString(int count) {
        String countStr;
        if (count > 100000) {
            countStr = String.format(Locale.ROOT, "%.2e", (double) count);
            String shulkcount = count / 1728 + translateComponent("sducarpet.text.count.unit_box").getString() + count % 1728 / 64 + translateComponent("sducarpet.text.count.unit_stack").getString() + count % 64 + translateComponent("sducarpet.text.count.unit_item").getString();
            countStr = countStr + "(" + shulkcount + ")";
        } else if (count > 5000) {
            countStr = String.valueOf(count);
            String shulkcount = count / 1728 + translateComponent("sducarpet.text.count.unit_box").getString() + count % 1728 / 64 + translateComponent("sducarpet.text.count.unit_stack").getString() + count % 64 + translateComponent("sducarpet.text.count.unit_item").getString();
            countStr = countStr + "(" + shulkcount + ")";
        } else {
            countStr = String.valueOf(count);
        }
        return countStr;
    }
}
