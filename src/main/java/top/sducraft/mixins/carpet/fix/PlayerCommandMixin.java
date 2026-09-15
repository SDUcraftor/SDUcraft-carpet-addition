package top.sducraft.mixins.carpet.fix;

import carpet.commands.PlayerCommand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.sducraft.SDUcraftCarpetSettings;

import java.util.Optional;

//#if MC >= 260100
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;

import java.util.UUID;
//#else
//$$ import com.mojang.authlib.GameProfile;
//$$ import net.minecraft.server.players.GameProfileCache;
//#endif

@Mixin(PlayerCommand.class)
public class PlayerCommandMixin {

    //#if MC >= 260100
    @WrapOperation(method = "cantSpawn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/UserNameToIdResolver;get(Ljava/util/UUID;)Ljava/util/Optional;"))
    private static Optional<NameAndId> getProfileCacheWrap(UserNameToIdResolver instance, UUID uuid, Operation<Optional<NameAndId>> original) {
        return SDUcraftCarpetSettings.OfflineBots ? Optional.empty() : original.call(instance, uuid);
    }
    //#else
    //$$ @WrapOperation(method = "cantSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/GameProfileCache;get(Ljava/lang/String;)Ljava/util/Optional;"))
    //$$ private static Optional<GameProfile> getProfileCacheWrap(GameProfileCache instance, String string, Operation<Optional<GameProfile>> original) {
    //$$     return SDUcraftCarpetSettings.OfflineBots ? Optional.empty() : original.call(instance, string);
    //$$ }
    //#endif
}
