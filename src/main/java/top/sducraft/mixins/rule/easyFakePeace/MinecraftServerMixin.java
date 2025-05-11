package top.sducraft.mixins.rule.easyFakePeace;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static top.sducraft.helpers.rule.fakePeaceHelper.FakePeaceHelper.loadChunkOnInitialize;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    @Inject(method = "onTickRateChanged",at=@At("HEAD"))
    private void onTickRateChanged(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer)(Object)this;
        loadChunkOnInitialize(server);
    }
}
