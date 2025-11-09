package top.sducraft.commands.finditem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import top.sducraft.config.findItemArea.FindItemAreaData;

import static top.sducraft.util.Message.translateComponent;

public class FindItemAreaCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("finditemarea")
                .requires(source -> source.hasPermission(2)) // Require op level 2
                .then(Commands.literal("add")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .then(Commands.argument("from", BlockPosArgument.blockPos())
                                        .then(Commands.argument("to", BlockPosArgument.blockPos())
                                                .executes(context -> {
                                                    String name = StringArgumentType.getString(context, "name");
                                                    BlockPos from = BlockPosArgument.getBlockPos(context, "from");
                                                    BlockPos to = BlockPosArgument.getBlockPos(context, "to");
                                                    boolean success = FindItemAreaData.addArea(name, new FindItemAreaData.AreaData(from, to));
                                                    if (success) {
                                                        context.getSource().sendSuccess(() -> translateComponent("sducarpet.command.finditemarea.added").append(" '" + name + "'"), true);
                                                    } else {
                                                        context.getSource().sendFailure(translateComponent("sducarpet.command.finditemarea.exists").append(" '" + name + "'"));
                                                    }
                                                    return success ? 1 : 0;
                                                })
                                        )
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    FindItemAreaData.getAreas().keySet().forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    boolean success = FindItemAreaData.removeArea(name);
                                    if (success) {
                                        context.getSource().sendSuccess(() -> translateComponent("sducarpet.command.finditemarea.removed").append(" '" + name + "'"), true);
                                    } else {
                                        context.getSource().sendFailure(translateComponent("sducarpet.command.finditemarea.notFound").append(" '" + name + "'"));
                                    }
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("list")
                        .executes(context -> {
                            var areas = FindItemAreaData.getAreas();
                            if (areas.isEmpty()) {
                                context.getSource().sendSuccess(() -> translateComponent("sducarpet.command.finditemarea.empty"), false);
                            } else {
                                context.getSource().sendSuccess(() -> translateComponent("sducarpet.command.finditemarea.list"), false);
                                areas.forEach((name, data) -> context.getSource().sendSuccess(() -> Component.literal("- " + name), false));
                            }
                            return areas.size();
                        })
                )
        );
    }
}