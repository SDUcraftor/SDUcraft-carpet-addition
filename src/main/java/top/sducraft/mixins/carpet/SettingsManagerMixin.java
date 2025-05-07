package top.sducraft.mixins.carpet;

import carpet.api.settings.SettingsManager;
import carpet.utils.Messenger;
import carpet.utils.TranslationKeys;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static carpet.utils.Translations.tr;

@Mixin(SettingsManager.class)
public class SettingsManagerMixin {
    @Inject(method = "listAllSettings", at = @At(value = "INVOKE",target = "Lcarpet/utils/Translations;tr(Ljava/lang/String;)Ljava/lang/String;",ordinal = 1,shift = At.Shift.AFTER),remap = false)
    private void listAllSettings(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        String mod_version = "1.2.4";
        Messenger.m(source, "g "+"Carpet SDUcraft Addition"+" "+ tr(TranslationKeys.VERSION) + ": " + mod_version);
    }

}
