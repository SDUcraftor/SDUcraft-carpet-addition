package top.sducraft.commands.syncmaticaCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import top.sducraft.config.allItemData.AllItemData;
import top.sducraft.helpers.commands.allItemCommand.SpawnDisplay;
import top.sducraft.helpers.litematica.LoadSyncmatica;

import java.io.File;
import java.util.Set;

import static carpet.utils.Translations.tr;
import static top.sducraft.helpers.litematica.LoadSyncmatica.*;
import static top.sducraft.helpers.litematica.SyncmaticaCommandHelper.getBlockNames;
import static top.sducraft.helpers.litematica.SyncmaticaCommandHelper.listSyncmatica;
import static top.sducraft.util.DelayedEventScheduler.addScheduleEvent;

public class SyncmaticaCommand {
            public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
                            commandDispatcher.register(Commands.literal("syncmatica")
                                    .then(Commands.literal("reload")
                                            .executes(context -> {
                                                if(loadSyncmatica(context.getSource().getServer())){
                                                    context.getSource().sendSuccess(() -> Component.literal(tr("Syncmatica has been reloaded ")), false);
                                                    return 1;
                                                }
                                                else {
                                                    context.getSource().sendFailure(Component.literal(tr("Fail to reload Syncmatica")));
                                                    return 0;
                                                }
                                            }))
                                    .then(Commands.literal("list")
                                            .executes(context -> {
                                                ServerPlayer player = context.getSource().getPlayer();
                                                if (player != null) {
                                                    listSyncmatica(player,1);
                                                    return 1;
                                                }
                                                else {
                                                    return 0;
                                                }
                                            }).then(Commands.argument("page", IntegerArgumentType.integer(0))
                                                    .executes(context -> {
                                                        ServerPlayer  player = context.getSource().getPlayer();
                                                        if (player != null) {
                                                            listSyncmatica(player,IntegerArgumentType.getInteger(context,"page"));
                                                            return 1;
                                                        }
                                                        else {
                                                            return 0;
                                                        }
                                                    })))
                                    .then(Commands.literal("material")
                                            .then(Commands.argument("syncmatic", StringArgumentType.greedyString())
                                                    .suggests((context, builder) -> suggestFuzzyNames(builder))
                                            .executes(context -> {

                                                LoadSyncmatica.Litematica litematica = LoadSyncmatica.getLitematica(StringArgumentType.getString(context, "syncmatic"));
                                                if (litematica != null) {
                                                    ServerPlayer  player = context.getSource().getPlayer();
                                                    File syncmatics = new File(context.getSource().getServer().getServerDirectory(), "syncmatics");
                                                    File litematicaFile = new File(syncmatics, litematica.hash.toString()+".litematic");
                                                    if (player != null) {
                                                        player.displayClientMessage(Component.literal("[DEBUG] Trying to read litematica from: " + litematicaFile.getAbsolutePath()),false);//debug
                                                        player.displayClientMessage(Component.literal("[DEBUG] File exists? " + litematicaFile.exists()),false);//debug
                                                    }
                                                    Set<String> blockNames = getBlockNames(litematicaFile);
                                                    ServerLevel level = context.getSource().getServer().overworld();
                                                    for (String blockName : blockNames){ //debug
                                                        context.getSource().sendSuccess(() -> Component.literal(blockName), false);
                                                    }
                                                    for (String name : blockNames) {
                                                        AllItemData.ItemData data = AllItemData.search(name);
                                                        if (data != null) {
                                                            for (BlockPos pos : data.chestPos) {
                                                                SpawnDisplay.spawnBlockDisplay(level, pos, level.getBlockState(pos), 0x00ffff, "material");
                                                            }
                                                        }
                                                    }
                                                    context.getSource().sendSuccess(() -> Component.literal(tr("已高亮")+blockNames.size()+("个材料")), false);
                                                    return 1;
                                                }
                                                return 0;
                                            }))
                                            .then(Commands.literal("clear")
                                                    .executes(context -> {
                                                        for (ServerLevel level : context.getSource().getServer().getAllLevels()){
                                                        for (Entity entity : level.getEntities().getAll()) {
                                                            if(entity!=null && entity.getTags().contains("material")) {
                                                                addScheduleEvent(1, entity::discard);
                                                            }
                                                        }
                                                    }
                                                        return 1;}))
                                    ))
                            ;
                        }

}
