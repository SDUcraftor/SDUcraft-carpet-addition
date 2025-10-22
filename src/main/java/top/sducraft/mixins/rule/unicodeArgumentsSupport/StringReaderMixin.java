package top.sducraft.mixins.rule.unicodeArgumentsSupport;

import com.mojang.brigadier.StringReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static top.sducraft.SDUcraftCarpetSettings.unicodeArgumentsSupport;

@Mixin(StringReader.class)
public class StringReaderMixin {

    @Inject(method = "isAllowedInUnquotedString", at = @At("HEAD"), cancellable = true)
    private static void isAllowedInUnquotedString(char c, CallbackInfoReturnable<Boolean> cir) {
        if (unicodeArgumentsSupport) {
            cir.setReturnValue(cir.getReturnValueZ() || Character.isLetterOrDigit(c) || c > 0x7F); // 0x7F 是 ASCII 范围的结束符
        }
    }
}
