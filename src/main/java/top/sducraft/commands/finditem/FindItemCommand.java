package top.sducraft.commands.finditem;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.sducraft.helpers.commands.allItemCommand.SpawnDisplay;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class FindItemCommand {

    public record FoundInfo(String areaName, BlockPos pos, double distanceSq, Component name, int count, String type) {}

    public record MergedResult(List<String> areaNames, BlockPos pos, double distanceSq, Component name, int count, String type) {}

    public record GroupingKey(BlockPos pos, String name, String type) {}

    private record DisplayInfo(Component message, double distanceSq) {}


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("finditem")
                .then(Commands.argument("item", StringArgumentType.string())
                        .suggests(((
                                context, builder) -> ItemName.findItemSuggest(builder)
                        ))
                        .executes(context -> {
                                    Item item = ItemName.getItem(StringArgumentType.getString(context, "item"));
                                    if (item != null) {
                                        execute(context.getSource(), item, 128);
                                        return 1;
                                    } else {
                                        return 0;
                                    }
                                }
                        )
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 256))
                                .executes(context -> {
                                            Item item = ItemName.getItem(StringArgumentType.getString(context, "item"));
                                            if (item != null) {
                                                execute(context.getSource(), item, IntegerArgumentType.getInteger(context, "radius"));
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        }
                                )
                        )
                )
                .then(Commands.literal("detail")
                        .then(Commands.argument("group_id", UuidArgument.uuid())
                                .executes(FindItemCommand::executeDetail)
                        )
                )
                .then(Commands.literal("action")
                        .then(Commands.literal("lookandhighlight")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(FindItemCommand::executeLookHighlight)
                                )
                        )
                )
                .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                        .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                                .then(Commands.argument("itemfilter", StringArgumentType.string())
                                        .executes(context -> executeFiltered(context.getSource(),
                                                BlockPosArgument.getBlockPos(context, "pos1"),
                                                BlockPosArgument.getBlockPos(context, "pos2"),
                                                StringArgumentType.getString(context, "itemfilter"),
                                                "*" // Default container filter
                                        ))
                                        .then(Commands.argument("containerfilter", StringArgumentType.string())
                                                .executes(context -> executeFiltered(context.getSource(),
                                                        BlockPosArgument.getBlockPos(context, "pos1"),
                                                        BlockPosArgument.getBlockPos(context, "pos2"),
                                                        StringArgumentType.getString(context, "itemfilter"),
                                                        StringArgumentType.getString(context, "containerfilter")
                                                ))
                                        )
                                )
                        )
                )
        );
    }

    private static int execute(@NotNull CommandSourceStack source, Item targetItemInput, int radius) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack displayStack = new ItemStack(targetItemInput);
        long startTime = System.nanoTime();
        ItemFinder.FindResult result = ItemFinder.findItemsInArea(player, targetItemInput, radius);

        String commandUser = player.getGameProfile().getName();

        // 清除该玩家上一次的搜索结果缓存
        FindItemResultCache.clear(player.getUUID());

        // 1. 将所有找到的物品（玩家、容器、掉落物）转换成一个统一的流
        Stream<FoundInfo> rawResultsStream = Stream.of(
                result.players().stream()
                        .map(info -> new FoundInfo(info.areaName(), info.pos(), info.distanceSq(), info.playerName().copy().withStyle(ChatFormatting.AQUA), info.itemCount(), "player")),
                result.containers().stream()
                        .map(info -> new FoundInfo(info.areaName(), info.pos(), info.distanceSq(), info.containerName().copy().withStyle(ChatFormatting.AQUA), info.itemCount(), "container")),
                result.droppedItems().stream()
                        .map(info -> new FoundInfo(info.areaName(), info.pos(), info.distanceSq(), Component.literal("掉落物").withStyle(ChatFormatting.AQUA), info.itemCount(), "dropped_item"))
                ).flatMap(s -> s);

        // 2. 按位置、名称和类型对结果进行分组，合并区域名称
        List<MergedResult> mergedResults = new ArrayList<>(rawResultsStream.collect(
                Collectors.groupingBy(
                        res -> new GroupingKey(res.pos(), res.name().getString(), res.type()),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    FoundInfo first = list.get(0);
                                    List<String> areaNames = list.stream().map(FoundInfo::areaName).distinct().sorted().toList();
                                    return new MergedResult(areaNames, first.pos(), first.distanceSq(), first.name(), first.count(), first.type());
                                }
                        )
                )
        ).values());

        // 3. 将结果分类
        List<MergedResult> playerResults = mergedResults.stream().filter(r -> r.type().equals("player")).toList();
        List<MergedResult> containerResults = mergedResults.stream().filter(r -> r.type().equals("container")).toList();
        List<MergedResult> droppedItemResults = mergedResults.stream().filter(r -> r.type().equals("dropped_item")).toList();

        // 4. 对容器和掉落物进行二次分组 (聚类)
        final double GROUPING_DISTANCE_SQ = 25.0; // 5格范围
        List<List<MergedResult>> containerGroups = clusterResults(containerResults, GROUPING_DISTANCE_SQ);
        List<List<MergedResult>> droppedItemGroups = clusterResults(droppedItemResults, GROUPING_DISTANCE_SQ);

        // 5. 创建最终的显示列表
        List<DisplayInfo> displayInfos = new ArrayList<>();

        // 添加玩家 (不分组)
        playerResults.forEach(res -> displayInfos.add(new DisplayInfo(createSingleResultMessage(res, displayStack, commandUser), res.distanceSq())));

        // 添加容器群组
        processGroups(containerGroups, displayInfos, displayStack, player, "container");

        // 添加掉落物群组
        processGroups(droppedItemGroups, displayInfos, displayStack, player, "dropped_item");

        if (displayInfos.isEmpty()) {
            source.sendSuccess(() -> Component.literal("未找到 '").append(displayStack.getHoverName()).append("'物品。"), false);
        } else {
            player.sendSystemMessage(Component.literal("--- 在" + displayInfos.size() + "个位置找到 ").append(displayStack.getHoverName()).append(" ---").withStyle(ChatFormatting.GOLD));

            displayInfos.sort(Comparator.comparingDouble(DisplayInfo::distanceSq));
            displayInfos.forEach(info -> player.sendSystemMessage(info.message()));

            source.sendSuccess(() -> Component.literal("使用他人物品前请先询问!!!!!").withStyle(ChatFormatting.RED), false);
        }

        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;
        source.sendSuccess(() -> Component.literal(String.format("搜索完成，耗时 %.2f 毫秒。", durationMs)), false);
        return displayInfos.size();
    }

    private static int executeDetail(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        UUID groupId = context.getArgument("group_id", UUID.class);
        List<MergedResult> group = FindItemResultCache.getGroup(player.getUUID(), groupId);

        if (group == null) {
            context.getSource().sendFailure(Component.literal("该物品组的详细信息已过期或不存在。"));
            return 0;
        }

        ItemStack displayStack = new ItemStack(net.minecraft.world.item.Items.STONE); // 占位符

        player.sendSystemMessage(Component.literal("--- 群组详细信息 ---").withStyle(ChatFormatting.GOLD));
        group.sort(Comparator.comparingDouble(MergedResult::distanceSq));
        for (MergedResult res : group) {
            player.sendSystemMessage(createSingleResultMessage(res, displayStack, player.getGameProfile().getName()));
        }
        return 1;
    }

    private static int executeLookHighlight(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");

        SpawnDisplay.spawnTempBlockDisplay(
                player.level(),
                pos,
                player.level().getBlockState(pos),
                0xFFFF00, // Yellow glow
                "finditem_highlight",
                300
        );

        player.lookAt(EntityAnchorArgument.Anchor.EYES,new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));

        return 1;
    }

    private static List<List<MergedResult>> clusterResults(List<MergedResult> results, double groupingDistanceSq) {
        List<List<MergedResult>> groups = new ArrayList<>();
        if (results.isEmpty()) {
            return groups;
        }

        List<MergedResult> remaining = new ArrayList<>(results);
        while (!remaining.isEmpty()) {
            List<MergedResult> newGroup = new ArrayList<>();
            LinkedList<MergedResult> toProcess = new LinkedList<>();

            MergedResult first = remaining.remove(0);
            newGroup.add(first);
            toProcess.add(first);

            while (!toProcess.isEmpty()) {
                MergedResult current = toProcess.poll();
                remaining.removeIf(other -> {
                    if (current.pos().distSqr(other.pos()) <= groupingDistanceSq) {
                        newGroup.add(other);
                        toProcess.add(other);
                        return true;
                    }
                    return false;
                });
            }
            groups.add(newGroup);
        }
        return groups;
    }

    private static void processGroups(List<List<MergedResult>> groups, List<DisplayInfo> displayInfos, ItemStack displayStack, ServerPlayer player, String groupType) {
        String commandUser = player.getGameProfile().getName();
        for (List<MergedResult> group : groups) {
            if (group.size() == 1) {
                MergedResult single = group.get(0);
                displayInfos.add(new DisplayInfo(createSingleResultMessage(single, displayStack, commandUser), single.distanceSq()));
            } else {
                UUID groupId = FindItemResultCache.cacheGroup(player.getUUID(), group);
                double closestDistSq = group.stream().mapToDouble(MergedResult::distanceSq).min().orElse(Double.MAX_VALUE);
                displayInfos.add(new DisplayInfo(createGroupResultMessage(group, groupId, closestDistSq, displayStack, groupType), closestDistSq));
            }
        }
    }

    private static Component createSingleResultMessage(MergedResult res, ItemStack displayStack, String commandUser) {
        double distance = Math.sqrt(res.distanceSq());
        BlockPos pos = res.pos();
        String command = String.format("/finditem action lookandhighlight %d %d %d", pos.getX(), pos.getY(), pos.getZ());
        String areaString = String.join(", ", res.areaNames());

        return Component.literal("- [" + areaString + "] ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("(%.1fm) ", distance)).withStyle(ChatFormatting.GRAY))
                .append(res.name())
                .append(Component.literal(" at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] ").withStyle(ChatFormatting.GRAY))
                .append(Component.literal("x" + res.count()) // x64
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.YELLOW)
                                .withHoverEvent(new HoverEvent.ShowItem(displayStack))))
                .append(Component.literal(" ").append(Component.literal("[点击看向目标]")
                        .withStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击看向目标位置")))
                                .withClickEvent(new ClickEvent.RunCommand(command))
                                .withColor(ChatFormatting.GOLD))));
    }

    private static Component createGroupResultMessage(List<MergedResult> group, UUID groupId, double closestDistSq, ItemStack displayStack, String groupType) {
        int totalCount = group.stream().mapToInt(MergedResult::count).sum();
        double distance = Math.sqrt(closestDistSq);
        String detailCommand = "/finditem detail " + groupId.toString();

        String groupName = switch (groupType) {
            case "container" -> group.size() + "个容器 ";
            case "dropped_item" -> group.size() + "个掉落物 ";
            default -> group.size() + "个群组 ";
        };

        return Component.literal("[+] ").withStyle(ChatFormatting.GREEN)
                .append(Component.literal(String.format("(%.1fm) ", distance)).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(groupName).withStyle(ChatFormatting.AQUA))
                .append(Component.literal("共 " + totalCount + " 个 ").withStyle(ChatFormatting.YELLOW)
                        .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowItem(displayStack))))
                .append(Component.literal("[点击展开]")
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.GOLD)
                                .withClickEvent(new ClickEvent.RunCommand(detailCommand))
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal("显示群组内所有物品的详细信息")))
                        )
                );
    }

    private static int executeFiltered(CommandSourceStack source, BlockPos pos1, BlockPos pos2, String itemFilterStr, String containerFilterStr) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        long startTime = System.nanoTime();

        BlockPos min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        BlockPos max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
        max = max.offset(1,1,1);
        AABB searchBox = new AABB(new Vec3(min.getX(), min.getY(),min.getZ()), new Vec3(max.getX(), max.getY(),max.getZ()));

        FilterParser.Filter itemFilter = FilterParser.parse(itemFilterStr);
        FilterParser.Filter containerFilter = FilterParser.parse(containerFilterStr);

        List<ItemFinder.FoundFilteredItemInfo> results = ItemFinder.findItemsWithFilters(player, searchBox, itemFilter, containerFilter);

        if (results.isEmpty()) {
            source.sendSuccess(() -> Component.literal("在指定区域和过滤器下未找到任何物品。"), false);
        } else {
            player.sendSystemMessage(Component.literal("--- 找到 " + results.size() + " 个符合条件的容器 ---").withStyle(ChatFormatting.GOLD));
            for (ItemFinder.FoundFilteredItemInfo info : results) {
                player.sendSystemMessage(createFilteredResultMessage(player, info));
            }
        }

        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;
        source.sendSuccess(() -> Component.literal(String.format("搜索完成，耗时 %.2f 毫秒。", durationMs)), false);

        return results.size();
    }

    private static Component createFilteredResultMessage(ServerPlayer player, ItemFinder.FoundFilteredItemInfo info) {
        double distance = Math.sqrt(info.distanceSq());
        BlockPos pos = info.containerPos();
        String commandUser = player.getGameProfile().getName();
        String command = String.format("/finditem action lookandhighlight %d %d %d", pos.getX(), pos.getY(), pos.getZ());

        MutableComponent message = Component.literal("- ")
                .append(info.containerName().copy().withStyle(ChatFormatting.AQUA))
                .append(Component.literal(String.format(" at [%d, %d, %d] (%.1fm)", pos.getX(), pos.getY(), pos.getZ(), distance)).withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" ").append(Component.literal("[点击看向目标]")
                        .withStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击看向容器位置")))
                                .withClickEvent(new ClickEvent.RunCommand(command))
                                .withColor(ChatFormatting.GOLD))));

        List<Map.Entry<Item, Integer>> sortedItems = new ArrayList<>(info.items().entrySet());
        sortedItems.sort(Map.Entry.<Item, Integer>comparingByValue().reversed());

        for (Map.Entry<Item, Integer> entry : sortedItems) {
            Item item = entry.getKey();
            int count = entry.getValue();
            ItemStack displayStack = new ItemStack(item);

            message.append(Component.literal("\n  - ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.literal("x" + count + " ").withStyle(ChatFormatting.YELLOW))
                    .append(item.getName(displayStack).copy().withStyle(ChatFormatting.WHITE)
                            .withStyle(Style.EMPTY.withHoverEvent(new HoverEvent.ShowItem(displayStack)))
                    );
        }
        return message;
    }
}