package top.sducraft.mixins.carpet.fix;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.GameProfileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.sducraft.SDUcraftCarpetSettings;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeMixin {

    @WrapOperation(method = "createFake", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/GameProfileCache;get(Ljava/lang/String;)Ljava/util/Optional;")
    )
    private static Optional<GameProfile> wrapCacheLookup(GameProfileCache instance, String string, Operation<Optional<GameProfile>> original) {
        if (SDUcraftCarpetSettings.OfflineBots) {
            return Optional.empty();
        }
        return original.call(instance, string);
    }

    @WrapOperation(method = "createFake", at = @At(value = "INVOKE", target = "Lcarpet/patches/EntityPlayerMPFake;fetchGameProfile(Ljava/lang/String;)Ljava/util/concurrent/CompletableFuture;", remap = false), remap = false)
    private static java.util.concurrent.CompletableFuture<?> fetchGameProfileWrap(String name, Operation<CompletableFuture<Optional<GameProfile>>> original) {
        return SDUcraftCarpetSettings.OfflineBots ? CompletableFuture.supplyAsync(Optional::empty) : original.call(name);
    }
}
