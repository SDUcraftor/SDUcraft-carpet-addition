package top.sducraft.mixins.carpet.fix;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.sducraft.SDUcraftCarpetSettings;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeMixin {

    /**
     * carpet 26.2 的 createFake 通过 fetchGameProfile(server, uuid) 解析玩家档案（会联网获取皮肤等）。
     * OfflineBots 开启时直接返回一个空名字的档案，createFake 会退回到本地构造的 GameProfile(uuid, username)，
     * 从而在不访问 Mojang 的情况下生成离线假人。
     */
    @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
            target = "Lcarpet/patches/EntityPlayerMPFake;fetchGameProfile(Lnet/minecraft/server/MinecraftServer;Ljava/util/UUID;)Ljava/util/concurrent/CompletableFuture;",
            remap = false), remap = false)
    private static CompletableFuture<GameProfile> fetchGameProfileWrap(MinecraftServer server, UUID uuid, Operation<CompletableFuture<GameProfile>> original) {
        if (SDUcraftCarpetSettings.OfflineBots) {
            return CompletableFuture.completedFuture(new GameProfile(uuid, ""));
        }
        return original.call(server, uuid);
    }
}
