package top.sducraft.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;

import static top.sducraft.helpers.commands.allItemCommand.ItemInfo.displayAllItemInfo;
import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;

public class PrimeBackupEasyCommand implements IEasyCommand{
    @Override
    public String getCommandName() {
        return "primebackup";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[备份系统]", "/easycommand primebackup","点击进入备份");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[pb 命令简介]").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://tisunion.github.io/PrimeBackup/zh/"))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看pb 命令简介介绍"))))
                .append(Component.literal("""
                        一个强大的 MCDR 备份插件，一套先进的 Minecraft 存档备份解决方案
                        """));
        player.displayClientMessage(component, false);
        player.displayClientMessage(Component.literal("输入 !!pb 或点击上方按钮获取更多详细信息"),false);
    }
}
