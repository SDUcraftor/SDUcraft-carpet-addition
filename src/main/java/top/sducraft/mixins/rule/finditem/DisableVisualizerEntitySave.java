package top.sducraft.mixins.rule.finditem;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Entity.class)
public abstract class DisableVisualizerEntitySave {
    @Shadow
    public abstract Set<String> getTags();

    @Inject(method = "saveAsPassenger", at = @At("HEAD"), cancellable = true)
    public void saveAsPassenger(CallbackInfoReturnable<Boolean> cir) {
        Set<String> tags = this.getTags();
        if (tags.contains("finditem_highlight")) {
            cir.setReturnValue(false);
        }
    }
}
