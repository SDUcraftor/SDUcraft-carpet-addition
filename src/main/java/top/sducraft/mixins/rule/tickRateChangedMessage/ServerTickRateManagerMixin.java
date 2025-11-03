package top.sducraft.mixins.rule.tickRateChangedMessage;

import net.minecraft.server.ServerTickRateManager;
import net.minecraft.world.TickRateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin(ServerTickRateManager.class)
public abstract class ServerTickRateManagerMixin extends TickRateManager {

    @Shadow
    public abstract void setTickRate(float f);

    @Inject(method = "requestGameToSprint", at = @At("HEAD"))
    private void RequestGameToSprint(int i, CallbackInfoReturnable<Boolean> cir) {
        if (SDUcraftCarpetSettings.tickRateChangedMessage) {
            this.setTickRate(20.0F);
        }
    }

    @Inject(method = "setFrozen", at = @At("HEAD"))
    private void setFrozen(boolean bl, CallbackInfo ci) {
        if (SDUcraftCarpetSettings.tickRateChangedMessage) {
            this.setTickRate(20.0F);
        }
    }
}
