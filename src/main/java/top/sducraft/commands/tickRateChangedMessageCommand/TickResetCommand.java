package top.sducraft.commands.tickRateChangedMessageCommand;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerTickRateManager;
import top.sducraft.SDUcraftCarpetSettings;
import static carpet.utils.Translations.tr;

public class TickResetCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("tick")
                .requires(c->SDUcraftCarpetSettings.easyCommand)
                .then(Commands.literal("reset")
                .executes(context -> {
                    ServerTickRateManager serverTickRateManager = context.getSource().getServer().tickRateManager();
                    serverTickRateManager.finishTickSprint();
                    serverTickRateManager.setTickRate(20);
                    serverTickRateManager.setFrozen(false);
                    context.getSource().sendSuccess(() -> Component.literal(tr("carpet.command.resetTick")), true);
                    return 1;
                })));
    }
}
