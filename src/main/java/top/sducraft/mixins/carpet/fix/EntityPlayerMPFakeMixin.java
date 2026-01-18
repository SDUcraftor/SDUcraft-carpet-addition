package top.sducraft.mixins.carpet.fix;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.litematica.LoadSyncmatica;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeMixin {

    @WrapOperation(method = "createFake", at = @At(value = "INVOKE", target = "Lcarpet/patches/EntityPlayerMPFake;fetchGameProfile(Ljava/lang/String;)Ljava/util/concurrent/CompletableFuture;"), remap = false)
    private static java.util.concurrent.CompletableFuture<?> fetchGameProfileWrap(String name, Operation<CompletableFuture<Optional<GameProfile>>> original) {
        return SDUcraftCarpetSettings.OfflineBots ? CompletableFuture.completedFuture(Optional.empty()) : original.call(name);
    }
}
