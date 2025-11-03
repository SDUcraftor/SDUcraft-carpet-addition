package top.sducraft.mixins.carpet.fix;

import carpet.CarpetSettings;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    protected abstract boolean shouldCheckPlayerMovement(boolean bl);

    @Redirect(method = "handleMovePlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;shouldCheckPlayerMovement(Z)Z"))
    private boolean shouldCheckPlayerMovement1(ServerGamePacketListenerImpl instance, boolean bl) {
        return CarpetSettings.antiCheatDisabled && this.shouldCheckPlayerMovement(bl);
    }

}
