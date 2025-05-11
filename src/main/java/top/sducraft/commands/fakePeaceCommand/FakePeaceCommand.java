package top.sducraft.commands.fakePeaceCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.commands.easyFakePeace.EasyFakePeaceCommandHelper;
import java.util.Objects;

import static top.sducraft.helpers.commands.easyFakePeace.EasyFakePeaceCommandHelper.showFakePeaceStatus;

public class FakePeaceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fakepeace")
                .requires(c -> SDUcraftCarpetSettings.easyFakePeace)
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    showFakePeaceStatus(Objects.requireNonNull(source.getPlayer()));
                    return 1;
                })
                .then(Commands.argument("dimension", DimensionArgument.dimension())
                        .then(Commands.argument("state", BoolArgumentType.bool())
                                .executes(context -> EasyFakePeaceCommandHelper.setFakePeaceState(
                                        context.getSource(),
                                        DimensionArgument.getDimension(context, "dimension"),
                                        BoolArgumentType.getBool(context, "state")
                                )))));

    }
}



