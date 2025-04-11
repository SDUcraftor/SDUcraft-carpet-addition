package top.sducraft.mixins.rule.netherPortalCooldown;


import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin (Entity.class)
public abstract class entityMixin {
    @Inject(method = "getDimensionChangingDelay", at = @At("HEAD"), cancellable = true)
    public void getPortalWaitTime(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(SDUcraftCarpetSettings.netherPortalCooldown);
    }
}
