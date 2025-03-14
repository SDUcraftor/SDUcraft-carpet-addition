package top.sducraft.mixins.rule.chickenJockeyGenerationOdds;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.sducraft.SDUcraftCarpetSettings;

@Mixin(Zombie.class)
public abstract class ZombieMixin {

   @ModifyConstant(method = "finalizeSpawn",constant = @Constant(doubleValue = 0.05,ordinal = 1))
   public double finalizeSpawn1(double constant){
      return SDUcraftCarpetSettings.chickenJockeyGenerationOdds;
   }

   @ModifyConstant(method = "finalizeSpawn",constant = @Constant(doubleValue = 0.05,ordinal = 0))
   public double finalizeSpawn2(double constant){
      if(SDUcraftCarpetSettings.chickenJockeyGenerationOdds!=0.05){
         return 0;
      }
      else {
         return constant;
      }
   }

   @Inject(method = "getSpawnAsBabyOdds",at=@At("HEAD"), cancellable = true)
   private static void getSpawnAsBabyOdds(RandomSource randomSource, CallbackInfoReturnable<Boolean> cir) {
      cir.setReturnValue(randomSource.nextFloat()< SDUcraftCarpetSettings.babyZombieOdds);
   }

}
