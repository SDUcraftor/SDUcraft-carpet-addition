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
import java.util.List;

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
        List<ItemFinder.FoundPlayerInfo> players = result.players();
        List<ItemFinder.FoundContainerInfo> containers = result.containers();
        List<ItemFinder.FoundDroppedItemInfo> droppedItems = result.droppedItems();
        long endTime = System.nanoTime();
        double durationMs = (endTime - startTime) / 1_000_000.0;

        if (players.isEmpty() && containers.isEmpty() && droppedItems.isEmpty()) {
            source.sendSuccess(() -> Component.literal("在半径 " + radius + " 格内未找到 '").append(displayStack.getHoverName()).append("'物品。"), false);
        } else {
            if (!players.isEmpty()) {
                player.sendSystemMessage(Component.literal("--- 找到 " + players.size() + " 个持有").append(displayStack.getHoverName()).append("的玩家 ---").withStyle(ChatFormatting.GREEN));
                for (ItemFinder.FoundPlayerInfo info : players) {
                    BlockPos pos = info.pos();
                    double distance = Math.sqrt(info.distanceSq());
                    String playerName = player.getGameProfile().getName();
                    String command;
                    command = String.format("/player %s look at %d %d %d", playerName, pos.getX(), pos.getY(), pos.getZ());

                    Component message = Component.literal(String.format(" - (%.1fm) ", distance))
                            .withStyle(Style.EMPTY
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击看向目标位置")))
                                    .withClickEvent(new ClickEvent.RunCommand(command))
                                    .withColor(ChatFormatting.GRAY)
                            )
                            .append(info.playerName().copy().withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(" at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] ")
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("x" + info.itemCount())
                                    .withStyle(Style.EMPTY
                                            .withColor(ChatFormatting.YELLOW)
                                            .withHoverEvent(new HoverEvent.ShowItem(displayStack)))
                            );
                    player.sendSystemMessage(message);
                }
            }

            if (!containers.isEmpty()) {
                player.sendSystemMessage(Component.literal("--- 找到 " + containers.size() + " 个包含").append(displayStack.getHoverName()).append("的容器 ---").withStyle(ChatFormatting.GREEN));
                for (ItemFinder.FoundContainerInfo info : containers) {
                    BlockPos pos = info.pos();
                    double distance = Math.sqrt(info.distanceSq());
                    String playerName = player.getGameProfile().getName();
                    String command = String.format("/player %s look at %d %d %d", playerName, pos.getX(), pos.getY(), pos.getZ());

                    Component message = Component.literal(String.format(" - (%.1fm) ", distance))
                            .withStyle(Style.EMPTY
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击看向目标位置")))
                                    .withClickEvent(new ClickEvent.RunCommand(command))
                                    .withColor(ChatFormatting.GRAY)
                            )
                            .append(info.containerName().copy().withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(" at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] ")
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("x" + info.itemCount())
                                    .withStyle(Style.EMPTY
                                            .withColor(ChatFormatting.YELLOW)
                                            .withHoverEvent(new HoverEvent.ShowItem(displayStack)))
                            );
                    player.sendSystemMessage(message);
                }
            }

            if (!droppedItems.isEmpty()) {
                player.sendSystemMessage(Component.literal("--- 找到 " + droppedItems.size() + " 个").append(displayStack.getHoverName()).append("掉落物 ---(仅显示最近15个)").withStyle(ChatFormatting.GREEN));
                int i = 0;
                for (ItemFinder.FoundDroppedItemInfo info : droppedItems) {
                    BlockPos pos = info.pos();
                    double distance = Math.sqrt(info.distanceSq());
                    String playerName = player.getGameProfile().getName();
                    String command = String.format("/player %s look at %d %d %d", playerName, pos.getX(), pos.getY(), pos.getZ());

                    Component message = Component.literal(String.format(" - (%.1fm) ", distance))
                            .withStyle(Style.EMPTY
                                    .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击看向目标位置")))
                                    .withClickEvent(new ClickEvent.RunCommand(command))
                                    .withColor(ChatFormatting.GRAY)
                            )
                            .append(Component.literal("掉落物").withStyle(ChatFormatting.AQUA))
                            .append(Component.literal(" at [" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "] ")
                                    .withStyle(ChatFormatting.GRAY))
                            .append(Component.literal("x" + info.itemCount())
                                    .withStyle(Style.EMPTY
                                            .withColor(ChatFormatting.YELLOW)
                                            .withHoverEvent(new HoverEvent.ShowItem(displayStack)))
                            );
                    player.sendSystemMessage(message);
                    i++;
                    if (i == 15) {
                        break;
                    }
                }
            }
            source.sendSuccess(() -> Component.literal("使用他人物品前请先询问!!!!!").withStyle(ChatFormatting.RED), false);
        }
        // 发送耗时信息

        source.sendSuccess(() -> Component.literal(String.format("搜索完成，耗时 %.2f 毫秒。", durationMs)), false);
        return players.size() + containers.size() + droppedItems.size();
    }
}