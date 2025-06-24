package top.sducraft.mixins.rule.disableNetherPortal;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PortalProcessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin(Entity.class)
public abstract class EntityMixin
{

    @Shadow @Nullable public PortalProcessor portalProcess;

    @Inject(method = "handlePortal",at=@At("HEAD"))
    protected void handleNetherPortal(CallbackInfo ci) {
        if(SDUcraftCarpetSettings.disableNetherPortal) {
            this.portalProcess = null;
        }
    }
}
