package top.sducraft.mixins.rule.visualizers.hopperVisualizers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;
import static top.sducraft.helpers.visualizers.HopperCooldownVisualizing.setVisualizer;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {

    @Inject(method = "pushItemsTick",at = @At("TAIL"))
    private static void pushItemsTick(Level level, BlockPos blockPos, BlockState blockState, HopperBlockEntity hopperBlockEntity, CallbackInfo ci) {
        if(level instanceof ServerLevel serverLevel && SDUcraftCarpetSettings.hopperCooldownVisualize) {
            setVisualizer(serverLevel, blockPos, hopperBlockEntity.cooldownTime);
            Direction direction = (Direction)blockState.getValue(HopperBlock.FACING);
            set(serverLevel, blockPos.relative(direction));
        }
    }

    @Unique
    private static void set(ServerLevel level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if(blockEntity instanceof HopperBlockEntity hopperBlockEntity) {
            setVisualizer(level, blockPos,hopperBlockEntity.cooldownTime);
        }
    }
}
