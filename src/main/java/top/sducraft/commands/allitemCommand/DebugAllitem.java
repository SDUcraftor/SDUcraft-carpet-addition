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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import top.sducraft.config.allItemData.AllItemData;

import javax.swing.plaf.IconUIResource;
import java.util.*;

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

                                    for (Map.Entry<String, AllItemData.itemData> entry :  AllItemData.dataList.entrySet()) {
                                        String descriptionId = entry.getKey();
                                        AllItemData.itemData data = entry.getValue();
                                        if (data.chestPos != null) {
                                                    Item item = getItemByDescriptionId(descriptionId);
                                                    if (item != null) {
                                                        spawnItemDisplay(level, data.chestPos, item, 0xFFFF00);
                                                     }
                                        }
                                    }

                                    context.getSource().sendSuccess(() -> Component.literal("已生成chest展示实体"), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("all")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel level = player.serverLevel();

                                    for (Map.Entry<String, AllItemData.itemData> entry :  AllItemData.dataList.entrySet()) {
                                        String descriptionId = entry.getKey();
                                        AllItemData.itemData data = entry.getValue();

                                        Set<BlockPos> store = new HashSet<>(data.storePos != null ? data.storePos : Set.of());
                                        Set<BlockPos> chest = data.chestPos != null ? data.chestPos : Set.of();
                                        store.removeAll(chest);

                                                Item item = getItemByDescriptionId(descriptionId);
                                                if (item != null) {
                                                    spawnItemDisplay(level, chest, item, 0xFFFF00);
                                                }

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

    public static boolean spawnItemDisplay(ServerLevel level, Set<BlockPos> chestPos, Item item, int color) {
        BlockPos bestSpawnPos = null;
        int maxAirNeighbors = -1;

        for (BlockPos pos : chestPos) {
            if (!(level.getBlockEntity(pos) instanceof Container)) continue;

            for (Direction dir : Direction.values()) {
                BlockPos candidatePos = pos.relative(dir);

                int airNeighbors = 0;
                for (Direction dir2 : Direction.values()) {
                    if (level.isEmptyBlock(candidatePos.relative(dir2))) {
                        airNeighbors++;
                    }
                }

                if (airNeighbors > maxAirNeighbors) {
                    maxAirNeighbors = airNeighbors;
                    bestSpawnPos = candidatePos;
                } else if (airNeighbors == maxAirNeighbors) {
                    if (bestSpawnPos == null
                            || candidatePos.getX() < bestSpawnPos.getX()
                            || (candidatePos.getX() == bestSpawnPos.getX() && candidatePos.getY() < bestSpawnPos.getY())
                            || (candidatePos.getX() == bestSpawnPos.getX() && candidatePos.getY() == bestSpawnPos.getY() && candidatePos.getZ() < bestSpawnPos.getZ())) {
                        bestSpawnPos = candidatePos;
                    }
                }
            }
        }

        if (bestSpawnPos == null) return false;

        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
        display.setPos(Vec3.atCenterOf(bestSpawnPos));
        display.setItemStack(new ItemStack(item));
        display.setGlowingTag(true);
        display.addTag("allitem_debug");
        display.getEntityData().set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, color);
        display.getEntityData().set(Display.DATA_SCALE_ID, new Vector3f(0.5F));
        level.addFreshEntity(display);
        return true;
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
