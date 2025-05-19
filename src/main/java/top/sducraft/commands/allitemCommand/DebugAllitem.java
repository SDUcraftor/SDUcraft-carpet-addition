package top.sducraft.commands.allitemCommand;

import carpet.CarpetSettings;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import top.sducraft.config.allItemData.AllItemData;

import javax.swing.plaf.IconUIResource;
import java.util.*;

import static top.sducraft.config.allItemData.AllItemData.dataList;
import static top.sducraft.util.DelayedEventScheduler.addScheduleEvent;

public class DebugAllitem {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("debug")
                .then(Commands.literal("allitem")
                        .then(Commands.literal("store")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel level = player.serverLevel();
                                    Map<String, AllItemData.itemData> dataMap = Objects.equals(CarpetSettings.language, "zh_cn") ?
                                            AllItemData.chineseNameToData : AllItemData.englishNameToData;

                                    for (AllItemData.itemData data : dataMap.values()) {
                                        if (data.storePos != null) {
                                            for (BlockPos pos : data.storePos) {
                                                if (level.getBlockEntity(pos) instanceof Container) {
                                                    BlockState state = level.getBlockState(pos);
                                                    spawnBlockDisplay(level, pos, state, 0x00FF00);
                                                }
                                            }
                                        }
                                    }

                                    context.getSource().sendSuccess(() -> Component.literal("已生成store展示实体"), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("chest")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel level = player.serverLevel();
                                    generateDisplays(level);


                                    context.getSource().sendSuccess(() -> Component.literal("已生成chest展示实体"), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("all")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel level = player.serverLevel();
                                    generateDisplays(level);

                                    for (Map.Entry<String, AllItemData.itemData> entry :  dataList.entrySet()) {
                                        AllItemData.itemData data = entry.getValue();

                                        Set<BlockPos> store = new HashSet<>(data.storePos != null ? data.storePos : Set.of());
                                        Set<BlockPos> chest = data.chestPos != null ? data.chestPos : Set.of();
                                        store.removeAll(chest);

                                        for (BlockPos pos :store) {
                                            if (level.getBlockEntity(pos) instanceof Container) {
                                                BlockState state = level.getBlockState(pos);
                                                spawnBlockDisplay(level, pos, state, 0x00FF00);
                                            }
                                        }
                                    }
                                    context.getSource().sendSuccess(() -> Component.literal("已生成全部展示实体"), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("stop")
                                .executes(context -> {
                                    for (ServerLevel level : context.getSource().getServer().getAllLevels()){
                                        for (Entity entity : level.getEntities().getAll()) {
                                            if(entity!=null && entity.getTags().contains("allitem_debug")) {
                                                addScheduleEvent(1, entity::discard);
                                            }
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

    public static void generateDisplays(ServerLevel level) {
        List<BlockPos> referencePositions = new ArrayList<>();
        List<Map.Entry<String, AllItemData.itemData>> entries = new ArrayList<>(dataList.entrySet());

        // Step 1: Build reference list from every 20th item
        for (int i = 0; i < entries.size(); i += 20) {
            AllItemData.itemData data = entries.get(i).getValue();
            if (data.chestPos == null) continue;

            BlockPos best = null;
            int maxAir = -1;
            for (BlockPos chestPos : data.chestPos) {
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = chestPos.relative(dir);
                    if (!level.isEmptyBlock(neighbor)) continue;

                    int airCount = countAirInCube7x3x7(level, neighbor);
                    if (airCount > maxAir || (airCount == maxAir && isPreferredDirection(neighbor, best))) {
                        maxAir = airCount;
                        best = neighbor;
                    }
                }
            }
            if (best != null) referencePositions.add(best);
        }

        for (Map.Entry<String, AllItemData.itemData> entry : dataList.entrySet()) {
            String descriptionId = entry.getKey();
            AllItemData.itemData data = entry.getValue();
            Item item = getItemByDescriptionId(descriptionId);
            if (item == null || data.chestPos == null) continue;

            BlockPos bestCandidate = null;
            int bestScore = -1;

            for (BlockPos chestPos : data.chestPos) {
                for (Direction dir : Direction.values()) {
                    BlockPos neighbor = chestPos.relative(dir);
                    int score = level.isEmptyBlock(neighbor)? 10 : 0 ;

                    int airCount = countAirInCube7x3x7(level, neighbor);
                    int correlation = calculateCorrelation(neighbor, referencePositions);
                    score += airCount + 3*correlation;

                    if (score > bestScore || (score == bestScore && isPreferredDirection(neighbor, bestCandidate))) {
                        bestScore = score;
                        bestCandidate = neighbor;
                    }
                }
            }

            if (bestCandidate != null) {
                spawnItemDisplay(level, bestCandidate, item, 0xFFFF00);
            }
        }
    }

    private static int calculateCorrelation(BlockPos pos, List<BlockPos> referenceList) {
        int correlation = 0;
        for (BlockPos ref : referenceList) {
            if (pos.getX() == ref.getX() || pos.getZ() == ref.getZ()) {
                if( pos.getY() == ref.getY()) correlation++;
            }
        }
        return correlation;
    }

    private static boolean isPreferredDirection(BlockPos a, BlockPos b) {
        if (b == null) return true;
        if (a.getX() != b.getX()) return a.getX() < b.getX();
        if (a.getY() != b.getY()) return a.getY() < b.getY();
        return a.getZ() < b.getZ();
    }

    private static void spawnItemDisplay(ServerLevel level, BlockPos pos, Item item, int color) {
        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
        display.setPos(Vec3.atCenterOf(pos));
        display.setItemStack(new ItemStack(item));
        display.setGlowingTag(true);
        display.addTag("allitem_debug");
        display.getEntityData().set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, color);
        display.getEntityData().set(Display.DATA_SCALE_ID, new Vector3f(0.5F));
        level.addFreshEntity(display);
    }

    private static int countAirInCube7x3x7(ServerLevel level, BlockPos center) {
        int count = 0;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                for (int dz = -3; dz <= 3; dz++) {
                    BlockPos checkPos = center.offset(dx, dy, dz);
                    if (level.isEmptyBlock(checkPos)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private static void spawnBlockDisplay(ServerLevel level, BlockPos pos, BlockState blockState, int color) {
        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        display.setBlockState(blockState);
        display.setPos(new Vec3(pos.getX(), pos.getY(), pos.getZ()));
        display.setGlowingTag(true);
        display.getEntityData().set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, color);
        display.addTag("allitem_debug");
        level.addFreshEntity(display);
    }

    public static Item getItemByDescriptionId(String descriptionId) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (new ItemStack(item).getDescriptionId().equals(descriptionId)) {
                return item;
            }
        }
        return null;
    }

}
