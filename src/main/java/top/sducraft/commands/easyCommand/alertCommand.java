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
import top.sducraft.helpers.commands.easyCommand.alertEasyCommand;
import top.sducraft.SDUcraftCarpetSettings;
import static top.sducraft.config.rule.alertConfig.*;

public class alertCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("alert")
                .requires(c -> SDUcraftCarpetSettings.easyCommand & c.hasPermission(2))
                .then(Commands.literal("add")
                .then(Commands.argument("name", StringArgumentType.string())
                .then(Commands.argument("dimension",DimensionArgument.dimension())
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .then(Commands.argument("text",StringArgumentType.string())
                        .executes(context ->{
                            if(addAlert(StringArgumentType.getString(context,"name"),DimensionArgument.getDimension(context,"dimension").dimension().location().getPath(),BlockPosArgument.getBlockPos(context,"pos"),StringArgumentType.getString(context,"text"),false))
                            {
                                context.getSource().sendSuccess(() -> Component.translatable("sducarpet.easycommand.alertcommand1").append(StringArgumentType.getString(context, "name")), false);
                             return 1;
                            }
                            else {
                                context.getSource().sendFailure(Component.translatable("sducarpet.easycommand.alertcommand6").append(StringArgumentType.getString(context,"name")));
                                return 0;
                            }
                        }))))))
                .then(Commands.literal("del")
                .requires(c -> SDUcraftCarpetSettings.easyCommand & c.hasPermission(2))
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((context, builder) ->{
                            builder.suggest("all")
;                            for (alert alert : alertList){
                                builder.suggest("\""+alert.name+"\"");
                            }
                            return builder.buildFuture();
                        })
                        .executes(context -> {
                            if(delAlert(StringArgumentType.getString(context,"name"),context.getSource().getPlayer())){
                                context.getSource().sendSuccess(()-> Component.translatable("sducarpet.easycommand.alertcommand2").append(StringArgumentType.getString(context,"name")),false);
                                return 1;
                            }
                            else{
                                context.getSource().sendFailure(Component.translatable("sducarpet.easycommand.alertcommand3").append(StringArgumentType.getString(context,"name")));
                                return 0;
                            }
                            })))
                .then(Commands.literal("set")
                .then(Commands.argument("name", StringArgumentType.string())
                        .suggests((context, builder) ->{
                            for (alert alert : alertList){
                                builder.suggest("\""+alert.name+"\"");
                            }
                            return builder.buildFuture();
                        })
                        .then(Commands.argument("status", BoolArgumentType.bool())
                        .executes(context -> {
                            if(setAlert(StringArgumentType.getString(context,"name"),BoolArgumentType.getBool(context,"status"))){
                                if(BoolArgumentType.getBool(context,"status")){
                                    context.getSource().sendSuccess(()->Component.translatable("sducarpet.easycommand.alertcommand3").append(Component.literal(StringArgumentType.getString(context,"name")).withStyle(Style.EMPTY.withColor(ChatFormatting.RED))),false);
                                }
                                else {
                                    context.getSource().sendSuccess(()->Component.translatable("sducarpet.easycommand.alertcommand4").append(Component.literal(StringArgumentType.getString(context,"name")).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))),false);
                                }
                                return 1;
                            }
                            else {
                                    context.getSource().sendFailure(Component.translatable("sducarpet.easycommand.alertcommand5").append("\""+StringArgumentType.getString(context,"name")+ "\""));
                                    return 0;
                            }
                        }))))
                        .then(Commands.literal("list")
                                .executes(context -> {
                                    alertEasyCommand.showAlertList(context.getSource().getPlayer());
                                    return 1;
                                })));
    }
}
