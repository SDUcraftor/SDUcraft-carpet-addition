package top.sducraft.mixins.rule.firework;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.FireworkParticles;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static top.sducraft.SDUcraftCarpetSettings.*;

@Environment(EnvType.CLIENT)
@Mixin(FireworkParticles.Starter.class)
public abstract class fireworkParticlesMixin {

    @Inject(method = "isFarAwayFromCamera",at = @At("HEAD"), cancellable = true)
    private void isFarAwayFromCamera(CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(true);
    }

    @ModifyArgs(method = "tick",at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/FireworkParticles$Starter;createParticleBall(DI[I[IZZ)V"))
    private void createParticleBall (Args args) {
        if (args.get(0) instanceof Double && args.get(1) instanceof Integer) {
            double size = (double) args.get(0);
            int particleCount = (int) args.get(1);
            if (size == 0.5 && particleCount == 4) {
                args.set(0, bigfireworkRange);
                args.set(1, bigfireworkParticleNumber);
            }
            if (size == 0.25 && particleCount == 2) {
                args.set(0, fireworkRange);
                args.set(1, fireworkParticleNumber);
            }
        }
    }

    @ModifyArgs(method = "tick",at= @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/FireworkParticles$Starter;createParticleShape(D[[D[I[IZZZ)V"))
    private void createParticleBall1 (Args args) {
          args.set(0, fireworkRange);
    }
}

