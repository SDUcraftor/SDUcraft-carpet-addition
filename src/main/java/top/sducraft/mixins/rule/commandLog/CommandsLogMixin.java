package top.sducraft.mixins.rule.commandLog;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin(value = Commands.class, priority = 999)
public class CommandsLogMixin {

    @Unique
    private static final Logger LOGGER1 = LoggerFactory.getLogger("Command");

    @Inject(method = "performCommand", at = @At("HEAD"))
    private void onPerformCommand(ParseResults<CommandSourceStack> parseResults, String string, CallbackInfo ci) {
        if (SDUcraftCarpetSettings.commandLog) {
            LOGGER1.info("{}{}{}", parseResults.getContext().getSource().getPlayer(), "run \"/", string + "\"");
        }
    }

}
