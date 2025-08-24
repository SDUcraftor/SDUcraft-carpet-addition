package top.sducraft.commands.finditem;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemFinder {

    public record FoundContainerInfo(BlockPos pos, Component containerName, int itemCount, double distanceSq) {}

    public record FoundDroppedItemInfo(BlockPos pos, int itemCount, double distanceSq) {}

    public record FindResult(List<FoundContainerInfo> containers, List<FoundDroppedItemInfo> droppedItems) {}

    public static FindResult findItemsInArea(ServerPlayer player, Item targetItem, int radius) {
        net.minecraft.server.level.ServerLevel level = player.level();
        BlockPos playerPos = player.blockPosition();
        List<FoundContainerInfo> foundContainers = new ArrayList<>();
        List<FoundDroppedItemInfo> foundDroppedItems = new ArrayList<>();

        // --- 1. 搜索方块实体容器 (例如: 箱子, 木桶) ---
        for (BlockPos currentPos : BlockPos.betweenClosed(
                playerPos.offset(-radius, -radius, -radius),
                playerPos.offset(radius, radius, radius))) {

            BlockEntity blockEntity = level.getBlockEntity(currentPos);

            if (blockEntity instanceof Container inventory) {
                int count = 0;
                // 遍历整个容器以统计物品总数
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack itemStack = inventory.getItem(i);
                    if (!itemStack.isEmpty() && itemStack.is(targetItem)) {
                        count += itemStack.getCount();
                    }
                }

                if (count > 0) {
                    // 找到了！添加容器信息
                    Component name = level.getBlockState(currentPos).getBlock().getName();
                    double distSq = playerPos.distSqr(currentPos);
                    foundContainers.add(new FoundContainerInfo(currentPos.immutable(), name, count, distSq));
                }
            }
        }

        // 创建一个用于实体搜索的包围盒
        AABB searchBox = new AABB(playerPos).inflate(radius);

        // --- 2. 搜索实体容器 (例如: 箱子矿车, 运输船) ---
        List<Entity> containerEntities = level.getEntitiesOfClass(Entity.class, searchBox, entity ->
                entity instanceof Container && !entity.isRemoved()
        );

        for (Entity entity : containerEntities) {
            Container inventory = (Container) entity;
            int count = 0;
            // 遍历整个容器以统计物品总数
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack itemStack = inventory.getItem(i);
                if (!itemStack.isEmpty() && itemStack.is(targetItem)) {
                    count += itemStack.getCount();
                }
            }

            if (count > 0) {
                // 找到了！添加实体信息
                Component name = entity.getName();
                double distSq = player.position().distanceToSqr(entity.position());
                foundContainers.add(new FoundContainerInfo(entity.blockPosition(), name, count, distSq));
            }
        }

        // --- 3. 搜索掉落物实体 ---
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, searchBox, entity ->
                !entity.isRemoved() && entity.getItem().is(targetItem)
        );

        // 将相同位置的掉落物分组，以合并它们的数量
        Map<BlockPos, List<ItemEntity>> groupedItems = itemEntities.stream()
                .collect(Collectors.groupingBy(Entity::blockPosition));

        for (Map.Entry<BlockPos, List<ItemEntity>> entry : groupedItems.entrySet()) {
            BlockPos pos = entry.getKey();
            List<ItemEntity> itemsAtPos = entry.getValue();
            // 计算该位置所有掉落物堆叠的总数
            int totalCount = itemsAtPos.stream().mapToInt(itemEntity -> itemEntity.getItem().getCount()).sum();

            if (totalCount > 0) {
                double distSq = playerPos.distSqr(pos);
                foundDroppedItems.add(new FoundDroppedItemInfo(pos, totalCount, distSq));
            }
        }


        // --- 4. 对结果进行排序 ---
        foundContainers.sort(Comparator.comparingDouble(FoundContainerInfo::distanceSq));
        foundDroppedItems.sort(Comparator.comparingDouble(FoundDroppedItemInfo::distanceSq));

        return new FindResult(foundContainers, foundDroppedItems);
    }
}