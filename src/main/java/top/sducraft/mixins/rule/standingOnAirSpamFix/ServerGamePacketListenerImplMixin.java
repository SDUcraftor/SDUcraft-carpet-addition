package top.sducraft.mixins.rule.standingOnAirSpamFix;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
//#if MC >= 260300
//$$ import net.minecraft.server.level.ServerPlayer;
//$$ import net.minecraft.world.entity.Entity;
//$$ import net.minecraft.world.phys.AABB;
//$$ import org.spongepowered.asm.mixin.Shadow;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$ import top.sducraft.SDUcraftCarpetSettings;
//#endif

/**
 * MC 26.3 在玩家上报 onGround 而服务端 verticalCollisionBelow 为 false 时, 会打 INFO 日志并强发
 * 脚下方块包 (forceSendPlayerSupportBlocks, 每 200 tick 限流一次). 但该标志只在发生向下位移并碰撞
 * 方块时才置位, 登录后直接站立/站在实体上的玩家会被永久误判, 每 10 秒刷屏一次.
 *
 * 规则开启时, 先在服务端侧复查玩家脚下是否真的没有支撑(方块碰撞或可碰撞实体): 有支撑说明是误报,
 * 跳过日志与强发; 确实悬空(幽灵方块)则保留原版修复逻辑.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    //#if MC >= 260300
    //$$ @Shadow
    //$$ public ServerPlayer player;
    //$$
    //$$ @Inject(method = "forceSendPlayerSupportBlocks", at = @At("HEAD"), cancellable = true)
    //$$ private void skipFalseStandingOnAir(CallbackInfo ci) {
    //$$     if (SDUcraftCarpetSettings.standingOnAirSpamFix) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //#endif
}
