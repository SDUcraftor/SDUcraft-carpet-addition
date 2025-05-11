package top.sducraft.mixins.rule.disableNetherPortal;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin(Entity.class)
public abstract class EntityMixin
{
    @Shadow protected boolean isInsidePortal;

    @Inject(method = "handleNetherPortal",at=@At("HEAD"))
    protected void handleNetherPortal(CallbackInfo ci) {
        if(SDUcraftCarpetSettings.disableNetherPortal) {
            this.isInsidePortal = false;
        }
    }
}
