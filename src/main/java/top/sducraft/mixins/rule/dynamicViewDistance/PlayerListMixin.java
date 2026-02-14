package top.sducraft.mixins.rule.dynamicViewDistance;

import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin(value = PlayerList.class,priority = 0)
public class PlayerListMixin {

    @Inject(method = "setViewDistance", at = @At("HEAD"), cancellable = true)
    public void setViewDistance(int i, CallbackInfo ci) {
        if(SDUcraftCarpetSettings.dynamicViewDistance){
            ci.cancel();
        }
    }
    
}
