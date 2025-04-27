package top.sducraft.mixins.rule.armorStandIgnoreShulkerDamage;


import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.sducraft.SDUcraftCarpetSettings;
@Mixin(ArmorStand.class)

public abstract class ArmorStandMixin extends LivingEntity{

    protected ArmorStandMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }
    @Inject(method = "hurt", at = @At(value = "HEAD"), cancellable = true)
    private void ShulkerBulletKillArmorStand(DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir){
     if (SDUcraftCarpetSettings.armorStandIgnoreShulkerDamage){
         Entity entity;
         entity = damageSource.getDirectEntity();
         if (entity != null && entity.getType() == EntityType.SHULKER_BULLET) {
             cir.setReturnValue(false);
         }
     }
    }
}
