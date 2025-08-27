package top.sducraft.commands.finditem;

import carpet.patches.EntityPlayerMPFake;
import net.fabricmc.fabric.api.entity.FakePlayer;
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

    public record FoundPlayerInfo(Component playerName, BlockPos pos, int itemCount, double distanceSq) {}
    public record FoundContainerInfo(BlockPos pos, Component containerName, int itemCount, double distanceSq) {}

    public record FoundDroppedItemInfo(BlockPos pos, int itemCount, double distanceSq) {}

    public record FindResult(List<FoundPlayerInfo> players, List<FoundContainerInfo> containers, List<FoundDroppedItemInfo> droppedItems) {}

    public static FindResult findItemsInArea(ServerPlayer player, Item targetItem, int radius) {
            net.minecraft.server.level.ServerLevel level = player.level();
        BlockPos playerPos = player.blockPosition();
        List<FoundPlayerInfo> foundPlayers = new ArrayList<>();
        List<FoundContainerInfo> foundContainers = new ArrayList<>();
        List<FoundDroppedItemInfo> foundDroppedItems = new ArrayList<>();

        // --- 1. 搜索方块实体容器 (例如: 箱子, 木桶) ---
        ServerChunkCache chunkSource = level.getChunkSource();
        int playerChunkX = player.chunkPosition().x;
        int playerChunkZ = player.chunkPosition().z;
        // 将方块半径转换为区块半径，并额外+1以覆盖边缘情况
        int chunkRadius = (radius >> 4) + 1;

        for (int cz = playerChunkZ - chunkRadius; cz <= playerChunkZ + chunkRadius; cz++) {
            for (int cx = playerChunkX - chunkRadius; cx <= playerChunkX + chunkRadius; cx++) {
                // 获取区块，但如果未加载则不强制加载，以避免产生新的区块加载卡顿
                LevelChunk chunk = chunkSource.getChunk(cx, cz, false);
                if (chunk == null) {
                    continue;
                }

                // 遍历这个区块中所有的方块实体
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos bePos = entry.getKey();
                    // 确保方块实体在球形搜索半径内
                    if (bePos.distSqr(playerPos) > (long)radius * radius) {
                        continue;
                    }

                    BlockEntity blockEntity = entry.getValue();
                    if (blockEntity instanceof Container container) {
                        int count = countItems(container, targetItem);
                        if (count > 0) {
                            foundContainers.add(new FoundContainerInfo(bePos.immutable(), blockEntity.getBlockState().getBlock().getName(), count, bePos.distSqr(playerPos)));
                        }
                    }
                }
            }
        }

        // 创建一个用于实体和玩家搜索的包围盒
        AABB searchBox = new AABB(playerPos).inflate(radius);

        // --- 2. 搜索实体容器 (例如: 箱子矿车, 运输船) ---
        List<Entity> containerEntities = level.getEntitiesOfClass(Entity.class, searchBox, entity ->
                entity instanceof Container && !entity.isRemoved()
        );

        for (Entity entity : containerEntities) {
            int count = countItems((Container) entity, targetItem);
            if (count > 0) {
                // 找到了！添加实体信息
                Component name = entity.getName();
                double distSq = player.position().distanceToSqr(entity.position());
                foundContainers.add(new FoundContainerInfo(entity.blockPosition(), name, count, distSq));
            }
        }

        // --- 3. 搜索附近玩家的物品栏 ---
        List<ServerPlayer> nearbyPlayers = level.getEntitiesOfClass(ServerPlayer.class, searchBox, p ->
                !p.getUUID().equals(player.getUUID()) && !p.isRemoved()
        );

        for (ServerPlayer otherPlayer : nearbyPlayers) {
            Container inventory = otherPlayer.getInventory();
            int count = countItems(inventory, targetItem);
            if (count > 0) {
                double distSq = player.position().distanceToSqr(otherPlayer.position());
                if (otherPlayer instanceof EntityPlayerMPFake player1) {
                   foundPlayers.add(new FoundPlayerInfo(Component.empty().append(otherPlayer.getDisplayName()).append("(假人)"), otherPlayer.blockPosition(), count, distSq));
                }
                else {
                    foundPlayers.add(new FoundPlayerInfo(otherPlayer.getDisplayName(), otherPlayer.blockPosition(), count, distSq));
                }
            }
        }

        // --- 4. 搜索掉落物实体 ---
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


        // --- 5. 对结果进行排序 ---
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