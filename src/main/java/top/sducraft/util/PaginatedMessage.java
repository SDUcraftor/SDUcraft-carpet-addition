package top.sducraft.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
import java.util.List;
import static carpet.utils.Translations.tr;

/**
 * Utility class for displaying paginated messages to players in Minecraft.
 * <p>
 * 用于向 Minecraft 玩家显示分页消息的工具类。
 *
 * <p>This class provides methods to display lists of items with automatic pagination,
 * including clickable navigation buttons for moving between pages.
 * <p>
 * 此类提供了显示带有自动分页的项目列表的方法，包括用于在页面之间导航的可点击按钮。
 */
public class PaginatedMessage {
    
    /**
     * Displays a paginated list of items to a player with customizable page navigation.
     * <p>
     * 向玩家显示带有可自定义页面导航的分页项目列表。
     *
     * <p><b>Features / 特性：</b>
     * <ul>
     *   <li>Automatic pagination when item count exceeds threshold / 当项目数量超过阈值时自动分页</li>
     *   <li>Clickable navigation buttons ("<-" and "->") / 可点击的导航按钮（"<-" 和 "->"）</li>
     *   <li>Page counter display (e.g., "第 1 页 / 共 3 页") / 页码计数器显示（例如："第 1 页 / 共 3 页"）</li>
     *   <li>Custom command template for page navigation / 用于页面导航的自定义命令模板</li>
     * </ul>
     *
     * <p><b>Usage Example / 使用示例：</b>
     * <pre>{@code
     * List<String> items = Arrays.asList("item1", "item2", "item3", ...);
     * PaginatedMessage.displayMessageWithPage(
     *     player,           // ServerPlayer
     *     1,                // Current page number (starts from 1)
     *     10,               // Items per page
     *     10,               // No paging threshold
     *     items,            // List of items
     *     item -> Component.literal(item), // Item to Component converter
     *     "/mycommand list" // Command template for navigation
     * );
     * }</pre>
     *
     * @param <T>                 The type of the list containing items / 包含项目的列表类型
     * @param <K>                 The type of individual items in the list / 列表中各个项目的类型
     * @param player              The player who will receive the messages / 将接收消息的玩家
     * @param page                The current page number (1-based). Will be clamped to valid range / 当前页码（从 1 开始）。将被限制在有效范围内
     * @param itemPerPage         Maximum number of items to display per page / 每页显示的最大项目数
     * @param noPagingThreshold   Minimum number of items required to enable pagination.
     *                           If total items ≤ this value, all items are shown without pagination /
     *                           启用分页所需的最小项目数。如果总项目数 ≤ 此值，则显示所有项目而不分页
     * @param items               The list of items to display / 要显示的项目列表
     * @param itemToComponent     Function to convert each item to a Component for display /
     *                           将每个项目转换为用于显示的 Component 的函数
     * @param commandTemplate     Command template for page navigation. Page number will be appended.
     *                           Example: "/loc list" will generate "/loc list 2" for page 2 /
     *                           用于页面导航的命令模板。
     *                           示例："/loc list" 将 2生成 "/loc list 2"
     *
     * @see net.minecraft.network.chat.Component
     * @see net.minecraft.network.chat.ClickEvent
     */
    public static <T extends List<K>,K> void displayMessageWithPage(ServerPlayer player, int page, int itemPerPage, int noPagingThreshold, T items, java.util.function.Function<K, Component> itemToComponent, String commandTemplate) {
        int totalItems = items.size();
        boolean paginate = totalItems > noPagingThreshold;
        int totalPages = paginate ? (totalItems + itemPerPage - 1) / itemPerPage : 1;
        page = Math.max(1, Math.min(page, totalPages));

        int startIndex = paginate ? (page - 1) * itemPerPage : 0;
        int endIndex = paginate ? Math.min(startIndex + itemPerPage, totalItems) : totalItems;

        for (int i = startIndex; i < endIndex; i++) {
            player.displayClientMessage(itemToComponent.apply(items.get(i)), false);
        }

        if (paginate) {
            if (page > 1 && page < totalPages) {
                player.displayClientMessage(Component.empty()
                        .append(Component.literal("<-").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(commandTemplate + " " + (page - 1))).withColor(ChatFormatting.AQUA)))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages)).append(Component.literal(" 页")))
                                .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(commandTemplate + " " + (page + 1))).withColor(ChatFormatting.AQUA)))), false);
            } else if (page == 1) {
                player.displayClientMessage(Component.literal(tr("第 "))
                        .append(Component.literal(String.valueOf(page)))
                        .append(Component.literal(tr(" 页 / 共 ")))
                        .append(Component.literal(String.valueOf(totalPages)))
                        .append(Component.literal(" 页"))
                        .append(Component.literal("->").withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(commandTemplate + " " + (page + 1))).withColor(ChatFormatting.AQUA))), false);
            } else {
                player.displayClientMessage(Component.empty()
                        .append(Component.literal("<-").withColor(0x7EFCFC).withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand(commandTemplate + " " + (page - 1)))))
                        .append(Component.literal(tr("第 ")).append(Component.literal(String.valueOf(page))).append(Component.literal(tr(" 页 / 共 ")))
                                .append(Component.literal(String.valueOf(totalPages))).append(Component.literal(" 页"))), false);
            }
        }
    }
}
