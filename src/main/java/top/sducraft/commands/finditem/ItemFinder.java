package top.sducraft.commands.finditem;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.server.level.ServerLevel;
import top.sducraft.config.findItemArea.FindItemAreaData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemFinder {

    public record FoundPlayerInfo(Component playerName, BlockPos pos, int itemCount, double distanceSq, String areaName) {}
    public record FoundContainerInfo(BlockPos pos, Component containerName, int itemCount, double distanceSq, String areaName) {}
    public record FoundDroppedItemInfo(BlockPos pos, int itemCount, double distanceSq, String areaName) {}

    public record FindResult(List<FoundPlayerInfo> players, List<FoundContainerInfo> containers, List<FoundDroppedItemInfo> droppedItems) {}

    private static void performSearchInBox(ServerLevel level, BlockPos centerForDistance, AABB searchBox, Item targetItem, String areaName, List<FoundContainerInfo> foundContainers, List<FoundDroppedItemInfo> foundDroppedItems) {
        ServerChunkCache chunkSource = level.getChunkSource();
        int minChunkX = (int)searchBox.minX >> 4;
        int maxChunkX = (int)searchBox.maxX >> 4;
        int minChunkZ = (int)searchBox.minZ >> 4;
        int maxChunkZ = (int)searchBox.maxZ >> 4;

        for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
            for (int cx = minChunkX; cx <= maxChunkX; cx++) {
                LevelChunk chunk = chunkSource.getChunk(cx, cz, true);
                if (chunk == null) {
                    continue;
                }
                // 遍历这个区块中所有的方块实体
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos bePos = entry.getKey();
                    // 确保方块实体在搜索框内
                    if (!searchBox.contains(bePos.getX() + 0.5, bePos.getY() + 0.5, bePos.getZ() + 0.5)) {
                        continue;
                    }

                    BlockEntity blockEntity = entry.getValue();
                    if (blockEntity instanceof Container container) {
                        int count = countItems(container, targetItem);
                        if (count > 0) {
                            foundContainers.add(new FoundContainerInfo(bePos.immutable(), blockEntity.getBlockState().getBlock().getName(), count, bePos.distSqr(centerForDistance), areaName));
                        }
                    }
                }
            }
        }

        // 创建一个用于实体和玩家搜索的包围盒
        // --- 2. 搜索实体容器 ---
        List<Entity> containerEntities = level.getEntitiesOfClass(Entity.class, searchBox, entity ->
                entity instanceof Container && !entity.isRemoved()
        );

        for (Entity entity : containerEntities) {
            int count = countItems((Container) entity, targetItem);
            if (count > 0) {
                Component name = entity.getName();
                double distSq = entity.position().distanceToSqr(centerForDistance.getX(), centerForDistance.getY(), centerForDistance.getZ());
                foundContainers.add(new FoundContainerInfo(entity.blockPosition(), name, count, distSq, areaName));
            }
        }

        // --- 3. 搜索掉落物实体 ---
        List<ItemEntity> itemEntities = level.getEntitiesOfClass(ItemEntity.class, searchBox, entity ->
                !entity.isRemoved() && entity.getItem().is(targetItem)
        );

        Map<BlockPos, List<ItemEntity>> groupedItems = itemEntities.stream()
                .collect(Collectors.groupingBy(Entity::blockPosition));

        for (Map.Entry<BlockPos, List<ItemEntity>> entry : groupedItems.entrySet()) {
            BlockPos pos = entry.getKey();
            List<ItemEntity> itemsAtPos = entry.getValue();
            int totalCount = itemsAtPos.stream().mapToInt(itemEntity -> itemEntity.getItem().getCount()).sum();

            if (totalCount > 0) {
                double distSq = centerForDistance.distSqr(pos);
                foundDroppedItems.add(new FoundDroppedItemInfo(pos, totalCount, distSq, areaName));
            }
        }
    }

    public static FindResult findItemsInArea(ServerPlayer player, Item targetItem, int radius) {
        net.minecraft.server.level.ServerLevel level = player.level();
        BlockPos playerPos = player.blockPosition();
        List<FoundPlayerInfo> foundPlayers = new ArrayList<>();
        List<FoundContainerInfo> foundContainers = new ArrayList<>();
        List<FoundDroppedItemInfo> foundDroppedItems = new ArrayList<>();

        String playerAreaName = "玩家身边";
        AABB playerSearchBox = new AABB(playerPos).inflate(radius);

        // 搜索容器和掉落物
        performSearchInBox(level, playerPos, playerSearchBox, targetItem, playerAreaName, foundContainers, foundDroppedItems);

        // --- 3. 搜索附近玩家的物品栏 ---
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, playerSearchBox, p ->
                !p.getUUID().equals(player.getUUID()) && !p.isRemoved()
        );

        for (ServerPlayer otherPlayer : nearbyPlayers) {
            Container inventory = otherPlayer.getInventory();
            int count = countItems(inventory, targetItem);
            if (count > 0) {
                double distSq = player.position().distanceToSqr(otherPlayer.position());
                if (otherPlayer instanceof EntityPlayerMPFake player1) {
                   foundPlayers.add(new FoundPlayerInfo(Component.empty().append(otherPlayer.getDisplayName()).append("(假人)"), otherPlayer.blockPosition(), count, distSq, playerAreaName));
                }
                else {
                    foundPlayers.add(new FoundPlayerInfo(otherPlayer.getDisplayName(), otherPlayer.blockPosition(), count, distSq, playerAreaName));
                }
            }
        }

        // --- 2. 搜索已定义的区域 ---
        Map<String, FindItemAreaData.AreaData> areas = FindItemAreaData.getAreas();
        for (Map.Entry<String, FindItemAreaData.AreaData> entry : areas.entrySet()) {
            String areaName = entry.getKey();
            AABB areaBox = entry.getValue().toAABB();
            performSearchInBox(level, playerPos, areaBox, targetItem, areaName, foundContainers, foundDroppedItems);
        }


        // --- 3. 对结果进行排序 ---
        foundPlayers.sort(Comparator.comparingDouble(FoundPlayerInfo::distanceSq));
        foundContainers.sort(Comparator.comparingDouble(FoundContainerInfo::distanceSq));
        foundDroppedItems.sort(Comparator.comparingDouble(FoundDroppedItemInfo::distanceSq));

        return new FindResult(foundPlayers, foundContainers, foundDroppedItems);
    }

    private static int countItems(Container container, Item targetItem) {
        int count = 0;
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack itemStack = container.getItem(i);
            if (!itemStack.isEmpty() && itemStack.is(targetItem)) {
                count += itemStack.getCount();
            }
        }
        return count;
    }
}