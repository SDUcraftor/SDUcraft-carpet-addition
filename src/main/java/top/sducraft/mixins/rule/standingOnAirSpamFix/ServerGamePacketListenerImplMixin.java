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
    //$$     if (SDUcraftCarpetSettings.standingOnAirSpamFix && this.hasServerSideSupport()) {
    //$$         ci.cancel();
    //$$     }
    //$$ }
    //$$
    //$$ private boolean hasServerSideSupport() {
    //$$     AABB box = this.player.getBoundingBox();
    //$$     // 与原版 force-send 相同的 1e-5 误差, 只检查脚底薄片, 避免身体与墙壁/天花板的接触被误算作支撑
    //$$     AABB feet = new AABB(box.minX, box.minY - 1.0E-5F, box.minZ, box.maxX, box.minY, box.maxZ);
    //$$     if (!this.player.level().noCollision(this.player, feet)) {
    //$$         return true;
    //$$     }
    //$$     // 船/潜影贝等可碰撞实体不参与方块碰撞, 单独检查脚下方 0.1 格内的支撑实体
    //$$     AABB entityZone = feet.expandTowards(0.0, -0.1, 0.0);
    //$$     for (Entity entity : this.player.level().getEntities(this.player, entityZone,
    //$$             other -> other != this.player && other.isAlive() && other.canBeCollidedWith(this.player))) {
    //$$         return true;
    //$$     }
    //$$     return false;
    //$$ }
    //#endif
}
