package top.sducraft.mixins.rule.firework;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ParticleEngine.class)
@Environment(EnvType.CLIENT)
public abstract class ParticleEngineMixin {
    @ModifyArg(
            method = {"method_18125"},
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/EvictingQueue;create(I)Lcom/google/common/collect/EvictingQueue;"
            ),
            remap = false
    )
    private static int modifyEvictingQueueSize(int original) {
        return 100000;
    }
}
