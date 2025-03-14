package top.sducraft.mixins.rule.projectileRaycastLength;

import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin(ProjectileUtil.class)
public abstract class ProjectileUtilMixin {
    @ModifyArg(method = "getHitResult",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;") )
        private static Vec3 getHitResult(Vec3 vec32) {
        if(SDUcraftCarpetSettings.projectileRaycastLength>0&& vec32.length()>SDUcraftCarpetSettings.projectileRaycastLength){
            vec32 = vec32.normalize();
            vec32 = vec32.multiply(SDUcraftCarpetSettings.projectileRaycastLength,SDUcraftCarpetSettings.projectileRaycastLength,SDUcraftCarpetSettings.projectileRaycastLength);
        }
        return vec32;
    }
}
