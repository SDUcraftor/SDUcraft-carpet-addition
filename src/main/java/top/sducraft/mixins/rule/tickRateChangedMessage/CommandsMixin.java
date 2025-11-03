package top.sducraft.mixins.rule.tickRateChangedMessage;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.commands.tickRateChangeMessage.TickRateChangeMessageCommandHelper;

import java.util.List;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(method = "performCommand", at = @At("HEAD"))
    private void onPerformCommand(ParseResults<CommandSourceStack> parseResults, String commandString, CallbackInfo ci) {
        if (SDUcraftCarpetSettings.tickRateChangedMessage) {
            List<ParsedCommandNode<CommandSourceStack>> nodes = parseResults.getContext().getNodes();

            if (!nodes.get(0).getNode().getName().equals("tick")) {
                return;
            }

            String subCommand = nodes.get(1).getNode().getName();
            CommandSourceStack source = parseResults.getContext().getSource();
            if (!(source.getEntity() instanceof ServerPlayer player)) {
                return;
            }

            switch (subCommand) {
                case "rate" -> {
                    try {
                        CommandContext<CommandSourceStack> context = parseResults.getContext().build(commandString);
                        float rateValue = context.getArgument("rate", Float.class);
                        TickRateChangeMessageCommandHelper.changeTickPlayName = (rateValue != 20.0f) ? player.getName().getString() : null;
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                case "warp", "freeze" ->
                        TickRateChangeMessageCommandHelper.changeTickPlayName = player.getName().getString();
                case "reset", "stop" -> TickRateChangeMessageCommandHelper.changeTickPlayName = null;
            }
        }
    }
}
