package top.sducraft.mixins.carpet.fix;

import carpet.commands.PlayerCommand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.players.GameProfileCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.sducraft.SDUcraftCarpetSettings;

import java.util.Optional;

@Mixin(PlayerCommand.class)
public class PlayerCommandMixin {

    @WrapOperation(method = "cantSpawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/GameProfileCache;get(Ljava/lang/String;)Ljava/util/Optional;"))
    private static Optional<GameProfile> getProfileCacheWrap(GameProfileCache instance, String string, Operation<Optional<GameProfile>> original) {
        return SDUcraftCarpetSettings.OfflineBots ? Optional.empty() : original.call(instance, string);
    }

}
