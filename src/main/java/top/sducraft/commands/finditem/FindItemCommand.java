package top.sducraft.commands.finditem;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class FindItemCommand {
    // 用于在合并前统一所有结果类型
    public record FoundInfo(String areaName, BlockPos pos, double distanceSq, Component name, int count, String type) {}
    // 用于存储合并后的最终结果
    public record MergedResult(List<String> areaNames, BlockPos pos, double distanceSq, Component name, int count, String type) {}
    // 用于对结果进行分组的键
    public record GroupingKey(BlockPos pos, String name, String type) {}
    // 用于最终排序和显示
    private record DisplayInfo(Component message, double distanceSq) {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("finditem")
                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                        .executes(context -> execute(context.getSource(), ItemArgument.getItem(context, "item"), 128)) // 默认半径 128
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 256))
                                .executes(context -> execute(context.getSource(), ItemArgument.getItem(context, "item"), IntegerArgumentType.getInteger(context, "radius")))
                        )
                )
                .then(Commands.literal("detail")
                        .then(Commands.argument("group_id", UuidArgument.uuid())
                                .executes(FindItemCommand::executeDetail)
                        )
                )
        );
    }

    private static int execute(@NotNull CommandSourceStack source, ItemInput targetItemInput, int radius) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        Item targetItem = targetItemInput.getItem();
        ItemStack displayStack = targetItemInput.createItemStack(1, false);
        long startTime = System.nanoTime();
        ItemFinder.FindResult result = ItemFinder.findItemsInArea(player, targetItem, radius);

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

        // 6. 排序并显示
        if (displayInfos.isEmpty()) {
            source.sendSuccess(() -> Component.literal("未找到 '").append(displayStack.getHoverName()).append("'物品。"), false);
        } else {
            player.sendSystemMessage(Component.literal("--- 在"+displayInfos.size()+"个位置找到 ").append(displayStack.getHoverName()).append(" ---").withStyle(ChatFormatting.GOLD));

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

        player.sendSystemMessage(Component.literal("--- 详细信息 ---").withStyle(ChatFormatting.GOLD));
        group.sort(Comparator.comparingDouble(MergedResult::distanceSq));
        for (MergedResult res : group) {
            player.sendSystemMessage(createSingleResultMessage(res, displayStack, player.getGameProfile().getName()));
        }
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
        String command = String.format("/player %s look at %d %d %d", commandUser, pos.getX(), pos.getY(), pos.getZ());
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
}