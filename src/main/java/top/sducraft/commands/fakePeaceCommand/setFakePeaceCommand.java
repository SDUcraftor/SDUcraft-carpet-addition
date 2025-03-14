package top.sducraft.commands.fakePeaceCommand;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import top.sducraft.SDUcraftCarpetSettings;
import static top.sducraft.config.rule.easyfakePeaceConfig.setFakePeaceCoordinates;

public class setFakePeaceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("setfakepeace")
                .requires(c -> SDUcraftCarpetSettings.easyFakePeace)
                .then(Commands.argument("dimension", DimensionArgument.dimension())
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .executes(context -> setFakePeaceCoordinates(DimensionArgument.getDimension(context, "dimension").dimension().toString(),BlockPosArgument.getBlockPos(context,"pos").getX(),BlockPosArgument.getBlockPos(context,"pos").getY(),BlockPosArgument.getBlockPos(context,"pos").getZ())
                               ))));
    }

}


