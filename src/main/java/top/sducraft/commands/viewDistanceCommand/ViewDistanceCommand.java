package top.sducraft.commands.viewDistanceCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.util.Message;

public class ViewDistanceCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("viewdistance")
            .executes(ViewDistanceCommand::checkViewDistance)
        );
    }

    private static int checkViewDistance(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        MinecraftServer server = source.getServer();
        int currentVd = server.getPlayerList().getViewDistance();

        boolean isDynamicEnabled = SDUcraftCarpetSettings.dynamicViewDistance;

        Component statusText = isDynamicEnabled
            ? Message.translateComponent("sducraft.view_distance.enabled").withStyle(ChatFormatting.GREEN)
            : Message.translateComponent("sducraft.view_distance.disabled").withStyle(ChatFormatting.RED);

        Component message = Component.empty()
            .append(Message.translateComponent("sducraft.view_distance.check_title"))
            .append("\n")
            .append(Message.translateComponent("sducraft.view_distance.current").withStyle(ChatFormatting.GRAY))
            .append(": ")
            .append(Component.literal(String.valueOf(currentVd)).withStyle(ChatFormatting.AQUA))
            .append("\n")
            .append(Message.translateComponent("sducraft.view_distance.dynamic_control").withStyle(ChatFormatting.GRAY))
            .append(": ")
            .append(statusText);

        source.sendSuccess(() -> message, false);

        return 1;
    }
}