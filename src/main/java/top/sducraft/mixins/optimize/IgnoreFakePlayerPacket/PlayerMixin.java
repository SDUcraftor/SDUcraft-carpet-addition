package top.sducraft.mixins.optimize.IgnoreFakePlayerPacket;

import carpet.patches.EntityPlayerMPFake;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.util.ISelf;

@Mixin(ServerPlayer.class)
public abstract class PlayerMixin implements ISelf<ServerPlayer> {
    @Inject(method = "sendSystemMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At("HEAD"), cancellable = true)
    private void ignorePacketToFakePlayer(Component component, boolean bl, CallbackInfo ci) {
        if (SDUcraftCarpetSettings.ignoreFakePlayerPacket && SduCarpet$self() instanceof EntityPlayerMPFake) {
            ci.cancel();
        }
    }
}

