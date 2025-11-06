package top.sducraft.mixins.rule.commandLog;

import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;

import static top.sducraft.SDUcraftCarpetAdditionMod.LOGGER;

@Mixin(value = Commands.class, priority = 1)
public abstract class CommandsMixin {
    @Inject(method = "performCommand", at = @At("HEAD"))
    private void commandLog(ParseResults<CommandSourceStack> parseResults, String commandString, CallbackInfo ci) {
        if (SDUcraftCarpetSettings.commandLog) {
            var context = parseResults.getContext();
            if (context == null) {
                LOGGER.info("UnknownSource ran \"/{}\"", commandString);
                return;
            }
            var source = context.getSource();
            String name = "UnknownSource";
            try {
                if (source.getEntity() != null) {
                    name = source.getEntity().toString();
                } else {
                    name = "Server/Console";
                }
            } catch (Throwable t) {
                name = "InvalidSource";
            }

            LOGGER.info("{} run \"/{}\"", name, commandString);
        }
    }
}
