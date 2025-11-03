package top.sducraft.mixins.rule.tickRateChangedMessage;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
            String changerName = TickRateChangeMessageCommandHelper.changeTickPlayName;

            if (tickRateManager.tickrate() != 20.0F || tickRateManager.isFrozen() || tickRateManager.isSprinting()) {
                if (changerName != null) {
                    for (ServerPlayer player : playerList.getPlayers()) {
                        if (player.getName().getString().equals(changerName)) {
                            player.displayClientMessage(Component.literal("检测到游戏速度改变成功，可以使用 ").append(Component.literal("/leavemessage").withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/leavemessage ")))).append(" 指令进行留言"), false);
                        } else {
                            player.displayClientMessage(Component.literal(changerName).withStyle(ChatFormatting.YELLOW).append(Component.literal(" 改变了游戏速度。").withStyle(ChatFormatting.WHITE)), false);
                        }
                    }
                }
            } else {
                playerList.broadcastSystemMessage(Component.literal("游戏速度已恢复正常。").withStyle(ChatFormatting.GREEN), false);
                TickRateChangeMessageCommandHelper.resetTickRateChangeMessage();
            }
        }
    }
}
