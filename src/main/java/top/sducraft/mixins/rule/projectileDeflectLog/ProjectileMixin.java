package top.sducraft.mixins.rule.projectileDeflectLog;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.util.ISelf;

//#if MC >= 260100
import net.minecraft.world.entity.EntityReference;
//#endif

@Mixin(Projectile.class)
public class ProjectileMixin implements ISelf<Projectile> {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectileMixin.class);

    //#if MC >= 260300
    //$$ @Inject(method = "deflect", at = @At("HEAD"))
    //$$ private void onDeflect(ProjectileDeflection projectileDeflection, Entity entity, EntityReference<Entity> entityReference, boolean bl, net.minecraft.world.phys.Vec3 vec3, CallbackInfoReturnable<Boolean> cir) {
    //$$     logDeflect(entity);
    //$$ }
    //#elseif MC >= 260100
    @Inject(method = "deflect", at = @At("HEAD"))
    private void onDeflect(ProjectileDeflection projectileDeflection, Entity entity, EntityReference<Entity> entityReference, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        logDeflect(entity);
    }
    //#else
    //$$ @Inject(method = "deflect", at = @At("HEAD"))
    //$$ private void onDeflect(ProjectileDeflection projectileDeflection, Entity entity, Entity entity2, boolean bl, CallbackInfoReturnable<Boolean> cir) {
    //$$     logDeflect(entity);
    //$$ }
    //#endif

    @Unique
    private void logDeflect(Entity entity) {
        if (SDUcraftCarpetSettings.projectileDeflectLog && entity instanceof Player player) {
            Projectile projectile = SduCarpet$self();
            LOGGER.info(" player" + player.getName() + "hit " + projectile.getType() + " at " + String.format("[%.2f,%.2f,%.2f]", projectile.getX(), projectile.getY(), projectile.getZ()));
        }
    }
}
