package top.sducraft.mixins.rule.finditem;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;
import top.sducraft.util.EntityTagConstants;

@Mixin(Entity.class)
public abstract class DisableVisualizerEntitySave {
    //#if MC >= 260100
    @Shadow
    public abstract Set<String> entityTags();
    //#else
    //$$ @Shadow
    //$$ public abstract Set<String> getTags();
    //#endif

    @Inject(method = "saveAsPassenger", at = @At("HEAD"), cancellable = true)
    public void saveAsPassenger(ValueOutput valueOutput, CallbackInfoReturnable<Boolean> cir) {
        //#if MC >= 260100
        Set<String> tags = this.entityTags();
        //#else
        //$$ Set<String> tags = this.getTags();
        //#endif
        if (tags.contains(EntityTagConstants.NO_SAVE)) {
            cir.setReturnValue(false);
        }
    }
}
