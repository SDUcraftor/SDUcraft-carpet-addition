package top.sducraft.commands.easyCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.Component;
import top.sducraft.SDUcraftCarpetSettings;
import java.util.Objects;
import static top.sducraft.config.rule.MachineStatusCommandConfig.*;
import static top.sducraft.easyCommand.MachineStatusCommand.showMachineStatus;

public class MachineStatusCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("machine")
                .requires(c -> SDUcraftCarpetSettings.easyCommand)
                .then(Commands.literal("add")
                .then(Commands.argument("type", StringArgumentType.string())
                        .suggests((context, builder) ->{
                             builder.suggest("temp");
                            if(Objects.requireNonNull(context.getSource().getPlayer()).hasPermissions(2)) {
                                builder.suggest("perm");
                            }
                             return builder.buildFuture();
                        })
                        .then(Commands.argument("name", StringArgumentType.string())
                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(context ->{
                            if(StringArgumentType.getString(context, "type").equals("perm")&& Objects.requireNonNull(context.getSource().getPlayer()).hasPermissions(2)){
                                addPermMachine(StringArgumentType.getString(context,"name"),BlockPosArgument.getBlockPos(context,"pos"),DimensionArgument.getDimension(context,"dimension").dimension().location().getPath());
                                context.getSource().sendSuccess(()-> Component.literal("已成功添加机器"+StringArgumentType.getString(context,"name")),false);
                            }
                            else if(StringArgumentType.getString(context,"type").equals("temp")) {
                                addTempMachine(StringArgumentType.getString(context,"name"),BlockPosArgument.getBlockPos(context,"pos"),DimensionArgument.getDimension(context,"dimension").dimension().location().getPath());
                                context.getSource().sendSuccess(()-> Component.literal("已成功添加临时机器"+StringArgumentType.getString(context,"name")),false);
                            }
                            return 1;
                        }
                        ))))))
                .then(Commands.literal("del")
                .then(Commands.argument("type", StringArgumentType.string())
                                .suggests((context, builder) ->{
                                        builder.suggest("temp");
                                        if(Objects.requireNonNull(context.getSource().getPlayer()).hasPermissions(2)) {
                                            builder.suggest("perm");
                                        }
                                        return builder.buildFuture();
                                })
                                .then(Commands.argument("name", StringArgumentType.string())
                                .suggests((context, builder) ->{
                                    if(StringArgumentType.getString(context, "type").equals("perm") && Objects.requireNonNull(context.getSource().getPlayer()).hasPermissions(2)){
                                        for(Machine machine : permMachineList) {
                                            builder.suggest(machine.name);
                                        }
                                    }
                                    else {
                                        for(Machine machine : tempMachineList ) {
                                            builder.suggest(machine.name);
                                        }
                                    }
                                        builder.suggest("all");
                                    return builder.buildFuture();
                                })
                                .executes(context ->{
                                      if(StringArgumentType.getString(context, "type").equals("perm")){
                                             delPermMachine(StringArgumentType.getString(context,"name"),context.getSource().getPlayer());
                                             }
                                             else if (StringArgumentType.getString(context, "type").equals("temp")){
                                             delTempMachine(StringArgumentType.getString(context,"name"),context.getSource().getPlayer());
                                             }
                                             return 1;
                                }))))
                .then(Commands.literal("status")
                        .executes(context -> showMachineStatus(Objects.requireNonNull(context.getSource().getPlayer())))));
    }
}
