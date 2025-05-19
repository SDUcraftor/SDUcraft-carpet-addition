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

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
                                            for (BlockPos pos :data.chestPos) {
                                                if ((level.getBlockEntity(pos) instanceof Container)) {
                                                    Item item = getItemByDescriptionId(descriptionId);
                                                    if (item != null) {
                                                        spawnItemDisplay(level, pos, item, 0xFFFF00);
                                                    }
                                                }
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

                                        Set<BlockPos> store = data.storePos != null ? data.storePos : Set.of();
                                        Set<BlockPos> chest = data.chestPos != null ? data.chestPos : Set.of();

                                        Set<BlockPos> all = new HashSet<>();
                                        all.addAll(store);
                                        all.addAll(chest);

                                        for (BlockPos pos : all) {
                                            if (!(level.getBlockEntity(pos) instanceof Container)) continue;

                                            if (chest.contains(pos)) {
                                                Item item = getItemByDescriptionId(descriptionId);
                                                if (item != null) {
                                                    spawnItemDisplay(level, pos, item, 0xFFFF00); // chest优先，黄光
                                                }
                                            } else if (store.contains(pos)) {
                                                BlockState state = level.getBlockState(pos);
                                                spawnBlockDisplay(level, pos, state, 0x00FF00); // store，绿光
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
                                                entity.discard();
                                            }
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

    private static void spawnItemDisplay(ServerLevel level, BlockPos pos, Item item, int color) {
        BlockPos spawnPos = null;
        for (Direction dir : Direction.values()) {
            BlockPos adjacent = pos.relative(dir);
            if (level.isEmptyBlock(adjacent)) {
                spawnPos = adjacent;
                break;
            }
        }
        if (spawnPos == null) spawnPos = pos;

        Display.ItemDisplay display = new Display.ItemDisplay(EntityType.ITEM_DISPLAY, level);
        display.setPos(Vec3.atCenterOf(spawnPos));
        display.setItemStack(new ItemStack(item));
        display.setGlowingTag(true);
        display.setCustomName(Component.literal(item.getDescription().getString()));
        display.setCustomNameVisible(true);
        display.addTag("allitem_debug");
        display.getEntityData().set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, color);
        display.getEntityData().set(Display.DATA_SCALE_ID,new Vector3f(0.5F));

        level.addFreshEntity(display);
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
