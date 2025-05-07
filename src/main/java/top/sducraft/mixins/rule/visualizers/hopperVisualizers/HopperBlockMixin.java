package top.sducraft.mixins.rule.visualizers.hopperVisualizers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.visualizers.HopperCooldownVisualizing;


@Mixin(HopperBlock.class)
public class HopperBlockMixin extends Block {

    public HopperBlockMixin(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(Level level, BlockPos blockPos, BlockState blockState, Player player) {
        if(level instanceof ServerLevel && SDUcraftCarpetSettings.hopperCooldownVisualize) {
            HopperCooldownVisualizing.removeVisualizer(blockPos);
        }
        return super.playerWillDestroy(level, blockPos, blockState, player);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, @Nullable LivingEntity livingEntity, ItemStack itemStack) {
        if(level instanceof ServerLevel && SDUcraftCarpetSettings.hopperCooldownVisualize) {
            HopperCooldownVisualizing.setVisualizer((ServerLevel) level, blockPos, -1);
        }
        super.setPlacedBy(level, blockPos, blockState, livingEntity, itemStack);
    }


}
