package top.sducraft.mixins.rule.tickRateChangedMessage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.helpers.commands.tickRateChangeMessage.TickRateChangeMessageCommandHelper;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

    @Shadow
    public abstract ServerTickRateManager tickRateManager();

    @Inject(method = "onTickRateChanged", at = @At("HEAD"))
    private void onTickRateChanged(CallbackInfo info) {
        if (SDUcraftCarpetSettings.tickRateChangedMessage) {
            MinecraftServer server = ((MinecraftServer) (Object) this);
            PlayerList playerList = server.getPlayerList();
            ServerTickRateManager tickRateManager = server.tickRateManager();
            if (tickRateManager().tickrate() != 20 || tickRateManager.isFrozen() || tickRateManager.isSprinting()) {
                for (ServerPlayer player : playerList.getPlayers()) {
                    if (player != null) {
                        player.displayClientMessage(Component.literal("检测到游戏速度发生改变，可以使用 ").append(Component.literal("/leavemessage").withStyle(ChatFormatting.AQUA)).append(" 指令进行留言"), false);
                    }
                }
            }
            if (tickRateManager.tickrate() == 20 && !tickRateManager.isFrozen() && !tickRateManager.isSprinting()) {
                TickRateChangeMessageCommandHelper.resetTickRateChangeMessage();
            }
        }
    }
}



