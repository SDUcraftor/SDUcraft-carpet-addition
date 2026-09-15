package top.sducraft.mixins.carpet.fix;

import carpet.patches.EntityPlayerMPFake;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.sducraft.SDUcraftCarpetSettings;

import java.util.concurrent.CompletableFuture;

//#if MC >= 260100
import net.minecraft.server.MinecraftServer;

import java.util.UUID;
//#else
//$$ import net.minecraft.server.players.GameProfileCache;
//$$
//$$ import java.util.Optional;
//#endif

@Mixin(EntityPlayerMPFake.class)
public class EntityPlayerMPFakeMixin {

    //#if MC >= 260100
    @WrapOperation(method = "createFake", at = @At(value = "INVOKE",
            target = "Lcarpet/patches/EntityPlayerMPFake;fetchGameProfile(Lnet/minecraft/server/MinecraftServer;Ljava/util/UUID;)Ljava/util/concurrent/CompletableFuture;",
            remap = false), remap = false)
    private static CompletableFuture<GameProfile> fetchGameProfileWrap(MinecraftServer server, UUID uuid, Operation<CompletableFuture<GameProfile>> original) {
        if (SDUcraftCarpetSettings.OfflineBots) {
            return CompletableFuture.completedFuture(new GameProfile(uuid, ""));
        }
        return original.call(server, uuid);
    }
    //#else
    //$$ @WrapOperation(method = "createFake", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/GameProfileCache;get(Ljava/lang/String;)Ljava/util/Optional;"))
    //$$ private static Optional<GameProfile> wrapCacheLookup(GameProfileCache instance, String string, Operation<Optional<GameProfile>> original) {
    //$$     return SDUcraftCarpetSettings.OfflineBots ? Optional.empty() : original.call(instance, string);
    //$$ }
    //$$
    //$$ @WrapOperation(method = "createFake", at = @At(value = "INVOKE", target = "Lcarpet/patches/EntityPlayerMPFake;fetchGameProfile(Ljava/lang/String;)Ljava/util/concurrent/CompletableFuture;", remap = false), remap = false)
    //$$ private static CompletableFuture<?> fetchGameProfileWrap(String name, Operation<CompletableFuture<Optional<GameProfile>>> original) {
    //$$     return SDUcraftCarpetSettings.OfflineBots ? CompletableFuture.supplyAsync(Optional::empty) : original.call(name);
    //$$ }
    //#endif
}
