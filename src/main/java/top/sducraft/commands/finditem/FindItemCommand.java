package top.sducraft.commands.finditem;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
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
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.Collectors;

public class FindItemCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        dispatcher.register(Commands.literal("finditem")
                .then(Commands.argument("item", ItemArgument.item(commandBuildContext))
                        .executes(context -> execute(context.getSource(), ItemArgument.getItem(context, "item"), 128)) // 默认半径 128
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, 256))
                                .executes(context -> execute(context.getSource(), ItemArgument.getItem(context, "item"), IntegerArgumentType.getInteger(context, "radius")))
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
        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;

        // 用于在合并前统一所有结果类型
        record FoundInfo(String areaName, BlockPos pos, double distanceSq, Component name, int count) {}
        // 用于存储合并后的最终结果
        record MergedResult(List<String> areaNames, BlockPos pos, double distanceSq, Component name, int count) {}
        // 用于对结果进行分组的键
        record GroupingKey(BlockPos pos, String name) {}

        // 1. 将所有找到的物品（玩家、容器、掉落物）转换成一个统一的流
        Stream<FoundInfo> rawResultsStream = Stream.of(
                        result.players().stream()
                                .map(info -> new FoundInfo(info.areaName(), info.pos(), info.distanceSq(), info.playerName().copy().withStyle(ChatFormatting.AQUA), info.itemCount())),
                        result.containers().stream()
                                .map(info -> new FoundInfo(info.areaName(), info.pos(), info.distanceSq(), info.containerName().copy().withStyle(ChatFormatting.AQUA), info.itemCount())),
                        result.droppedItems().stream()
                                .map(info -> new FoundInfo(info.areaName(), info.pos(), info.distanceSq(), Component.literal("掉落物").withStyle(ChatFormatting.AQUA), info.itemCount()))
                ).flatMap(s -> s);

        // 2. 按位置和名称对结果进行分组，合并区域名称
        List<MergedResult> allResults = new ArrayList<>(rawResultsStream.collect(
                Collectors.groupingBy(
                        res -> new GroupingKey(res.pos(), res.name().getString()),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                list -> {
                                    FoundInfo first = list.get(0);
                                    List<String> areaNames = list.stream().map(FoundInfo::areaName).distinct().sorted().toList();
                                    return new MergedResult(areaNames, first.pos(), first.distanceSq(), first.name(), first.count());
                                }
                        )
                )
        ).values());

        // 3. 按距离排序
        allResults.sort(Comparator.comparingDouble(MergedResult::distanceSq));

        if (allResults.isEmpty()) {
            source.sendSuccess(() -> Component.literal("在任何区域内都未找到 '").append(displayStack.getHoverName()).append("'物品。"), false);
        } else {
            player.sendSystemMessage(Component.literal("--- 在"+allResults.size()+"个位置找到 ").append(displayStack.getHoverName()).append(" ---").withStyle(ChatFormatting.GOLD));

            for (MergedResult res : allResults) {
                displayInfo(player, res.areaNames(), res.pos(), res.distanceSq(), res.name(), res.count(), displayStack);
            }
            source.sendSuccess(() -> Component.literal("使用他人物品前请先询问!!!!!").withStyle(ChatFormatting.RED), false);
        }
        // 发送耗时信息

        source.sendSuccess(() -> Component.literal(String.format("搜索完成，耗时 %.2f 毫秒。", durationMs)), false);
        return allResults.size();
    }

    private static void displayInfo(ServerPlayer player, List<String> areaNames, BlockPos pos, double distanceSq, Component name, int count, ItemStack displayStack) {
        double distance = Math.sqrt(distanceSq);
        String commandUser = player.getGameProfile().getName();
        String command = String.format("/player %s look at %d %d %d", commandUser, pos.getX(), pos.getY(), pos.getZ());

        String areaString = String.join(", ", areaNames);

        Component message = Component.literal("- [" + areaString + "] ").withStyle(ChatFormatting.AQUA)
                .append(Component.literal(String.format("(%.1fm) ", distance))
                        .withStyle(Style.EMPTY
                                .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击看向目标位置")))
                                .withClickEvent(new ClickEvent.RunCommand(command))
                                .withColor(ChatFormatting.GRAY)
                        ))
                .append(name)
                .append(Component.literal(" at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] ")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal("x" + count)
                        .withStyle(Style.EMPTY
                                .withColor(ChatFormatting.YELLOW)
                                .withHoverEvent(new HoverEvent.ShowItem(displayStack)))
                );
        player.sendSystemMessage(message);
    }
}