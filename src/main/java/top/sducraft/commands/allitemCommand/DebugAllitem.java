package top.sducraft.commands.allitemCommand;

import carpet.CarpetSettings;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import top.sducraft.config.allItemData.AllItemData;
import top.sducraft.helpers.commands.allItemCommand.SpawnDisplay;
import top.sducraft.util.DelayedEvents;

import java.util.*;

import static carpet.utils.Translations.tr;
import static top.sducraft.config.allItemData.AllItemData.dataList;
import static top.sducraft.helpers.commands.allItemCommand.SearchAllItem.suggestFuzzyItemNames;
import static top.sducraft.helpers.commands.allItemCommand.SpawnDisplay.spawnBlockDisplay;
import static top.sducraft.helpers.commands.allItemCommand.SpawnDisplay.spawnItemDisplay;

public class DebugAllitem {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("debug")
                .then(Commands.literal("allitem")
                        .then(Commands.literal("store")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel level = player.serverLevel();
                                    Map<String, AllItemData.ItemData> dataMap = Objects.equals(CarpetSettings.language, "zh_cn") ?
                                            AllItemData.chineseNameToData : AllItemData.englishNameToData;

                                    for (AllItemData.ItemData data : dataMap.values()) {
                                        if (data.storePos != null) {
                                            for (BlockPos pos : data.storePos) {
                                                if (level.getBlockEntity(pos) instanceof Container) {
                                                    BlockState state = level.getBlockState(pos);
                                                    spawnBlockDisplay(level, pos, state, 0x00FF00,"allitem_debug");
                                                }
                                            }
                                        }
                                    }

                                    context.getSource().sendSuccess(() -> Component.literal("已生成store展示实体"), false);
                                    return 1;
                                })
                                .then(Commands.argument("item", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestFuzzyItemNames(builder))
                                        .executes(context -> {
                                            ServerLevel level = context.getSource().getServer().overworld();
                                            AllItemData.ItemData data = AllItemData.search(StringArgumentType.getString(context, "item"));
                                            if (data != null) {

                                                Set<BlockPos> store = data.storePos;
                                                store.addAll(data.chestPos);
                                                for (BlockPos pos : store){
                                                    if (level.getBlockEntity(pos) instanceof Container) spawnBlockDisplay(context.getSource().getServer().overworld(), pos, context.getSource().getServer().overworld().getBlockState(pos), 0x00FF00, "allitem_debug");
                                                }
                                                context.getSource().sendSuccess(() -> Component.literal(tr("已生成")+StringArgumentType.getString(context, "item")+("展示实体")), false);
                                            }
                                            else {
                                                context.getSource().sendFailure(Component.literal(tr("未找到物品")));
                                            }
                                            return 0;
                                        })))
                        .then(Commands.literal("chest")
                                .executes(context -> {
                                    for (Map.Entry<String, AllItemData.ItemData> entry :  dataList.entrySet()) {
                                        AllItemData.ItemData data = entry.getValue();
                                        spawnItemDisplay(data, SpawnDisplay.DisplayType.PERM,"allitem_debug");
                                    }
                                    return 1;
                                })
                                .then(Commands.argument("item", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestFuzzyItemNames(builder))
                                        .executes(context -> {
                                            AllItemData.ItemData data = AllItemData.search(StringArgumentType.getString(context, "item"));
                                            if (data != null) {
                                                spawnItemDisplay(data, SpawnDisplay.DisplayType.PERM,"allitem_debug");
                                                context.getSource().sendSuccess(() -> Component.literal(tr("已生成")+StringArgumentType.getString(context, "item")+("展示实体")), false);
                                            }
                                            else {
                                                context.getSource().sendFailure(Component.literal(tr("未找到物品")));
                                            }
                                            return 0;
                                        })))
                        .then(Commands.literal("all")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ServerLevel level = player.serverLevel();

                                    for (Map.Entry<String, AllItemData.ItemData> entry :  dataList.entrySet()) {
                                        AllItemData.ItemData data = entry.getValue();

                                        Set<BlockPos> store = new HashSet<>(data.storePos != null ? data.storePos : Set.of());
                                        spawnItemDisplay(data, SpawnDisplay.DisplayType.PERM,"allitem_debug");

                                        for (BlockPos pos :store) {
                                            if (level.getBlockEntity(pos) instanceof Container) {
                                                BlockState state = level.getBlockState(pos);
                                                spawnBlockDisplay(level, pos, state, 0x00FF00, "allitem_debug");
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
                                                DelayedEvents.START_SERVER_TICK.register(1, s -> entity.discard());
                                            }
                                        }
                                    }
                                    return 1;
                                })
                        )
                )
        );
    }

}
