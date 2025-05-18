package top.sducraft.commands.allitemCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import top.sducraft.config.allItemData.AllItemData;

import static carpet.utils.Translations.tr;
import static top.sducraft.config.allItemData.AllItemData.search;
import static top.sducraft.helpers.commands.allItemCommand.AllItemCommandHelper.addItemToData;
import static top.sducraft.helpers.commands.allItemCommand.AllItemCommandHelper.deleteItemFromData;
import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.*;
import static top.sducraft.helpers.commands.allItemCommand.SearchAllItem.*;

// TODO 2025/5/6:全物品查找,全物品当前容量列表
public class AllItemCommand {
        public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
                commandDispatcher.register(Commands.literal("allitem")
                        .then(Commands.literal("data")
                        .requires(source -> source.hasPermission(2))
                                .then(Commands.literal("add")
                                .then(Commands.argument("type", StringArgumentType.string())
                                        .suggests((context, builder) ->{
                                            builder.suggest("bulk");
                                            builder.suggest("item");
                                            return builder.buildFuture();
                                        })
                                .then(Commands.argument("startpos", BlockPosArgument.blockPos())
                                .then(Commands.argument("endpos", BlockPosArgument.blockPos())
                                .then(Commands.argument("startstorepos", BlockPosArgument.blockPos())
                                .then(Commands.argument("endstorepos", BlockPosArgument.blockPos())
                                .then(Commands.argument("startchestpos", BlockPosArgument.blockPos())
                                .then(Commands.argument("endchestpos",BlockPosArgument.blockPos())
                                        .executes(context -> addItemToData(StringArgumentType.getString(context, "type"),
                                                BlockPosArgument.getBlockPos(context, "startpos"),
                                                BlockPosArgument.getBlockPos(context, "endpos"),
                                                BlockPosArgument.getBlockPos(context, "startstorepos"),
                                                BlockPosArgument.getBlockPos(context, "endstorepos"),
                                                BlockPosArgument.getBlockPos(context, "startchestpos"),
                                                BlockPosArgument.getBlockPos(context, "endchestpos"),
                                                context.getSource()))))))))))
                                .then(Commands.literal("del")
                                .then(Commands.argument("startpos", BlockPosArgument.blockPos())
                                .then(Commands.argument("endpos", BlockPosArgument.blockPos())
                                        .executes(context ->deleteItemFromData(BlockPosArgument.getBlockPos(context, "startpos"),
                                                BlockPosArgument.getBlockPos(context, "endpos"),
                                                context.getSource()))))))
                        .then(Commands.literal("search")
                                .then(Commands.argument("item", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestFuzzyItemNames(builder))
                                        .executes(context -> searchAndDisplay(StringArgumentType.getString(context,"item"),context.getSource()))))
                        .then(Commands.literal("info")
                                .executes(context -> displayAllItemInfo(context.getSource().getPlayer()))
                        .then(Commands.argument("item", StringArgumentType.greedyString())
                                .suggests((context, builder) -> suggestFuzzyItemNames(builder))
                                .executes(context -> {
                                    AllItemData.itemData data = search(StringArgumentType.getString(context,"item"));
                                    if(data !=null ){
                                        displayItemInfo(StringArgumentType.getString(context,"item"),data,context.getSource().getPlayer());
                                        return 1;
                                    }
                                    else {
                                        context.getSource().sendFailure(Component.literal(tr("未找到物品")));
                                        return 0;
                                    }
                                }))
                        .then(Commands.literal("lack")
                                .executes(context -> {
                                    displayLackItemInfoWithPage(context.getSource().getPlayerOrException(),1);
                                    return 1;
                                })
                        .then(Commands.argument("page", StringArgumentType.string())
                                                .executes(context -> {
                                                    displayLackItemInfoWithPage(context.getSource().getPlayerOrException(),Integer.parseInt(StringArgumentType.getString(context,"page")));
                                                return 1;})))
                        .then(Commands.literal("full")
                               .executes(context -> {
                                   displayFullItemInfoWithPage(context.getSource().getPlayerOrException(),1);
                                   return 1;
                               })
                        .then(Commands.argument("page", StringArgumentType.string())
                                        .executes(context -> {
                                            displayFullItemInfoWithPage(context.getSource().getPlayerOrException(),Integer.parseInt(StringArgumentType.getString(context,"page")));
                                            return 1;
                                        })))));
        }
}
