package top.sducraft.mixins.rule.projectileDeflectLog;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.util.ISelf;

@Mixin(Projectile.class)
public class ProjectileMixin implements ISelf<Projectile> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectileMixin.class);

    @Inject(method = "deflect", at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void onDeflect(ProjectileDeflection projectileDeflection, Entity entity, Entity entity2, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (SDUcraftCarpetSettings.projectileDeflectLog && entity instanceof Player player) {
            Projectile projectile = SduCarpet$self();
            LOGGER.info(" player" + player.getName() + "hit " + projectile.getType() + " at " + String.format("[%.2f,%.2f,%.2f]", projectile.getX(), projectile.getY(), projectile.getZ()));
        }
    }

}
