package top.sducraft.mixins.rule.itemPickupDelay;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import top.sducraft.SDUcraftCarpetSettings;

//修改丢出物品时的拾取延迟
@Mixin(Player.class)
public abstract class PlayerMixin extends Entity implements TraceableEntity {

    public PlayerMixin(EntityType<?> entityType, Level level) {super(entityType, level);}

    @ModifyArg(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;",at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/item/ItemEntity;setPickUpDelay(I)V"))
    public int drop(int i){
        return SDUcraftCarpetSettings.itempickupDelay;
    }

}