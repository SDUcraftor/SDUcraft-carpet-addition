package top.sducraft.mixins.carpet.fix;

import carpet.commands.PlayerCommand;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.players.UserNameToIdResolver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import top.sducraft.SDUcraftCarpetSettings;

import java.util.Optional;
import java.util.UUID;

@Mixin(PlayerCommand.class)
public class PlayerCommandMixin {

    /**
     * carpet 26.2 的 cantSpawn 用 UserNameToIdResolver#get(UUID) 查询正版档案。
     * OfflineBots 开启时返回空，使其走离线档案分支，不访问 Mojang。
     */
    @WrapOperation(method = "cantSpawn", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/players/UserNameToIdResolver;get(Ljava/util/UUID;)Ljava/util/Optional;"))
    private static Optional<NameAndId> getProfileCacheWrap(UserNameToIdResolver instance, UUID uuid, Operation<Optional<NameAndId>> original) {
        return SDUcraftCarpetSettings.OfflineBots ? Optional.empty() : original.call(instance, uuid);
    }
}
