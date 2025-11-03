package top.sducraft.mixins.rule.itemPickupDelay;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements TraceableEntity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyArg(method = "createItemStackToDrop", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setPickUpDelay(I)V"))
    private int drop(int i) {
        return SDUcraftCarpetSettings.itemPickUpDelay;
    }
}