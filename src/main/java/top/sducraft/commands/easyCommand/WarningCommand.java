package top.sducraft.commands.easyCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.commands.easyCommand.WarningEasyCommand;
import static carpet.utils.Translations.tr;
import static top.sducraft.config.rule.WarningConfig.*;

public class WarningCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("warning")
                .requires(c -> SDUcraftCarpetSettings.easyCommand)
                .then(Commands.literal("add")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.argument("name", StringArgumentType.string())
                .then(Commands.argument("dimension",DimensionArgument.dimension())
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("text",StringArgumentType.string())
                        .executes(context ->{
                            if(addWarning(StringArgumentType.getString(context,"name"),DimensionArgument.getDimension(context,"dimension").dimension().location().getPath(),BlockPosArgument.getBlockPos(context,"pos"),StringArgumentType.getString(context,"text"),false))
                            {
                                context.getSource().sendSuccess(() -> Component.literal(tr("sducarpet.easycommand.warningcommand1")).append(StringArgumentType.getString(context, "name")), false);
                             return 1;
                            }
                            else {
                                context.getSource().sendFailure(Component.literal(tr("sducarpet.easycommand.warningcommand6")).append(StringArgumentType.getString(context,"name")));
                                return 0;
                            }
                        }))))))
                .then(Commands.literal("del")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((context, builder) ->{
                            builder.suggest("all");
                            for (warning warning : warningList){
                                builder.suggest("\""+warning.name+"\"");
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if(delWarning(StringArgumentType.getString(context,"name"),context.getSource().getPlayer())){
                                context.getSource().sendSuccess(()-> Component.literal(tr("sducarpet.easycommand.warningcommand2")).append(StringArgumentType.getString(context,"name")),false);
                                return 1;
                            }
                            else{
                                context.getSource().sendFailure(Component.literal(tr("sducarpet.easycommand.warningcommand3")).append(StringArgumentType.getString(context,"name")));
                                return 0;
                            }
                            })))
                .then(Commands.literal("set")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((context, builder) ->{
                            for (warning warning : warningList){
                                builder.suggest("\""+warning.name+"\"");
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("status", BoolArgumentType.bool())
                        .executes(context -> {
                            if(setWarning(StringArgumentType.getString(context,"name"),BoolArgumentType.getBool(context,"status"))){
                                if(BoolArgumentType.getBool(context,"status")){
                                    context.getSource().sendSuccess(()->Component.literal(tr("sducarpet.easycommand.warningcommand3")).append(Component.literal(StringArgumentType.getString(context,"name")).withStyle(Style.EMPTY.withColor(ChatFormatting.RED))),false);
                                }
                                else {
                                    context.getSource().sendSuccess(()->Component.literal(tr("sducarpet.easycommand.warningcommand4")).append(Component.literal(StringArgumentType.getString(context,"name")).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))),false);
                                }
                                return 1;
                            }
                            else {
                                    context.getSource().sendFailure(Component.literal(tr("sducarpet.easycommand.warningcommand5")).append("\""+StringArgumentType.getString(context,"name")+ "\""));
                                    return 0;
                            }
                        }))))
                        .then(Commands.literal("list")
                                .executes(context -> {
                                    WarningEasyCommand.showWarningList(context.getSource().getPlayer());
                                    return 1;
                                })));
    }
}
