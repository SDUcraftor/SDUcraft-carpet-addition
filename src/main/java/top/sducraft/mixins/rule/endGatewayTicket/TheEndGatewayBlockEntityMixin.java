package top.sducraft.mixins.rule.endGatewayTicket;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;

import static top.sducraft.helpers.rule.chunkLoadHelper.RegistTicket.addEndGatewayTicket;

@Mixin(TheEndGatewayBlockEntity.class)
public abstract class TheEndGatewayBlockEntityMixin {

    @Inject(method = "teleportEntity",at =@At("HEAD"))
    private static void teleportEntity(Level level, BlockPos blockPos, BlockState blockState, Entity entity, TheEndGatewayBlockEntity theEndGatewayBlockEntity, CallbackInfo ci) {
        if (SDUcraftCarpetSettings.endGatewayTicket) {
            if (level instanceof ServerLevel && !theEndGatewayBlockEntity.isCoolingDown() && theEndGatewayBlockEntity.exitPortal != null) {
                BlockPos blockPos2;
                blockPos2 = TheEndGatewayBlockEntity.findExitPosition(level, theEndGatewayBlockEntity.exitPortal);
                addEndGatewayTicket((ServerLevel) level, new ChunkPos(blockPos2));
            }
        }
    }
}
