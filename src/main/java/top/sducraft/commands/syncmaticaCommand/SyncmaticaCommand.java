package top.sducraft.commands.syncmaticaCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import top.sducraft.util.DelayedEvents;

import java.io.File;
import java.util.Map;
import static carpet.utils.Translations.tr;
import static top.sducraft.helpers.litematica.LoadSyncmatica.*;
import static top.sducraft.helpers.litematica.MaterialListManenger.*;
import static top.sducraft.helpers.litematica.SyncmaticaCommandHelper.listSyncmatica;

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
                                                loadSyncmatica(context.getSource().getServer());
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
                                                        loadSyncmatica(context.getSource().getServer());
                                                        ServerPlayer  player = context.getSource().getPlayer();
                                                        if (player != null) {
                                                            listSyncmatica(player,IntegerArgumentType.getInteger(context,"page"));
                                                            return 1;
                                                        }
                                                        else {
                                                            return 0;
                                                        }
                                                    })))
                                    .then(Commands.literal("material").then(Commands.literal("load")
                                            .then(Commands.argument("syncmatic", StringArgumentType.greedyString())
                                                    .suggests((context, builder) -> suggestFuzzyNames(builder))
                                            .executes(context -> {

                                                Litematica litematica = getLitematica(StringArgumentType.getString(context, "syncmatic"));
                                                if (litematica != null) {
                                                    ServerPlayer  player = context.getSource().getPlayer();
                                                    File syncmatics = new File(context.getSource().getServer().getServerDirectory(), "syncmatics");
                                                    File litematicaFile = new File(syncmatics, litematica.hash.toString()+".litematic");
                                                    Map<String, Integer> blockCounts = getBlockCounts(litematicaFile);
                                                    ServerLevel level = context.getSource().getServer().overworld();
                                                    updateMaterial(blockCounts, player);
//                                                    if (player != null) {
//                                                        player.displayClientMessage(Component.literal(blockCounts.toString()), false);
//                                                    }
//                                                    context.getSource().sendSuccess(() -> Component.literal(tr("已高亮")+blockNames.size()+("个材料")), false);
                                                    return 1;
                                                }
                                                return 0;
                                            })))
                                            .then(Commands.literal("clear")
                                                    .executes(context -> {
                                                        for (ServerLevel level : context.getSource().getServer().getAllLevels()){
                                                        for (Entity entity : level.getEntities().getAll()) {
                                                            if(entity!=null && entity.getTags().contains("material")) {
                                                                DelayedEvents.START_SERVER_TICK.register(1, s -> entity.discard());
                                                            }
                                                        }
                                                    }
                                                        return 1;}))
                                            .then(Commands.literal("lack")
                                                    .executes(context -> {
                                                        ServerPlayer  player = context.getSource().getPlayer();
                                                        if(player != null) {
                                                            listLackMaterial(player,0);
                                                            return 1;
                                                        }
                                                        return 0;
                                                    })
                                                    .then(Commands.argument("page", IntegerArgumentType.integer(0))
                                                            .executes(context -> {
                                                                ServerPlayer  player = context.getSource().getPlayer();
                                                                if(player != null) {
                                                                    listLackMaterial(player,IntegerArgumentType.getInteger(context,"page"));
                                                                    return 1;
                                                                }
                                                                return 0;
                                                            })))
                                            .then(Commands.literal("missing")
                                                    .executes(context -> {
                                                        ServerPlayer  player = context.getSource().getPlayer();
                                                        if(player != null) {
                                                            listMissingMaterial(player,0);
                                                            return 1;
                                                        }
                                                        return 0;
                                                    })
                                                    .then(Commands.argument("page", IntegerArgumentType.integer(0))
                                                            .executes(context -> {
                                                                ServerPlayer  player = context.getSource().getPlayer();
                                                                if(player != null) {
                                                                    listMissingMaterial(player,IntegerArgumentType.getInteger(context,"page"));
                                                                    return 1;
                                                                }
                                                                return 0;
                                                            })))
                                    ));
                        }

}
