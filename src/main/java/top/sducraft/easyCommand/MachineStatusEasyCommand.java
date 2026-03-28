package top.sducraft.easyCommand;

import carpet.CarpetServer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.sducraft.config.rule.MachineStatusCommandConfig;
import top.sducraft.util.Message;
import top.sducraft.util.dialog.NoticeDialogBuilder;
import top.sducraft.util.dialog.TextComponentBuilder;

import java.util.Objects;

import static top.sducraft.config.rule.MachineStatusCommandConfig.permMachineList;
import static top.sducraft.config.rule.MachineStatusCommandConfig.tempMachineList;
import static top.sducraft.util.MassageComponentCreate.createCommandClickComponent;
import static top.sducraft.util.Message.translateComponent;

public class MachineStatusEasyCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "machinestatus";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[机器状态查询]", "/easycommand machinestatus", "点击进入机器状态查询界面");
    }

    private static ServerLevel getDimension(MinecraftServer server, String dimension) {
        ServerLevel level = null;
        switch (dimension) {
            case "overworld" -> level = server.getLevel(ServerLevel.OVERWORLD);
            case "nether" -> level = server.getLevel(ServerLevel.NETHER);
            case "end" -> level = server.getLevel(ServerLevel.END);
        }
        return level;
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {

        NoticeDialogBuilder builder = new NoticeDialogBuilder();

        builder.setTitle("Machine 指令介绍")
                .setCanCloseWithEscape(true); // 确保玩家可以按 Esc 关闭

        TextComponentBuilder commandsList = new TextComponentBuilder();
        commandsList.append("/machine status").color("aqua").withWidth(1000).append(" 查看当前机器状态\n").color("white");
        commandsList.append("/machine add <temp/perm> <name> <dimension> <blockPos>").color("aqua").append(" 添加机器\n").color("white");
        commandsList.append("/machine remove <temp/perm> <name>").color("aqua").append(" 删除机器").color("white").onClickRunCommand("/help");

        TextComponentBuilder description = new TextComponentBuilder(
                "非临时机器会自动获取坐标处开关状态，临时机器默认为开，使用完毕请删除(服务器关闭后临时机器不会保存)"
        ).color("#CCCCCC");

        TextComponentBuilder notice = new TextComponentBuilder(
                "注意事项: 非op仅能添加/删除临时机器"
        ).color("gold").bold();

        builder.addBody(commandsList);
        builder.addBody(description);
        builder.addBody(notice);

        Message.sandPlayerDialog(player, builder);
    }

    public static int showMachineStatus(ServerPlayer player) {
        boolean bl1 = tempMachineList.isEmpty();
        boolean bl2 = permMachineList.isEmpty();
        if (bl1 && bl2) {
            player.displayClientMessage(translateComponent("sducarpet.easycommand.machinestatus1"), false);
            return 0;
        } else {
            player.displayClientMessage(translateComponent("sducarpet.easycommand.machinestatus4").append(translateComponent("sducarpet.easycommand.machinestatus5").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))).append(translateComponent("sducarpet.easycommand.machinestatus6")).append(translateComponent("sducarpet.easycommand.machinestatus7").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))).append(translateComponent("sducarpet.easycommand.machinestatus8")), false);
            if (!bl1) {
                player.displayClientMessage(translateComponent("sducarpet.easycommand.machinestatus2"), false);
                for (MachineStatusCommandConfig.Machine machine : tempMachineList) {
                    player.displayClientMessage(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)).append(Component.literal("  " + machine.dimension + " (" + machine.pos.toShortString() + ")").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))), false);
                }
            }
            if (!bl2) {
                player.displayClientMessage(translateComponent("sducarpet.easycommand.machinestatus3"), false);
                for (MachineStatusCommandConfig.Machine machine : permMachineList) {
                    BlockState blockState = getDimension(player.getServer(), machine.dimension).getBlockState(machine.pos);
                    if ((blockState.getBlock() instanceof LeverBlock && blockState.getValue(LeverBlock.POWERED)) || (blockState.getBlock() instanceof RedstoneLampBlock && blockState.getValue(RedstoneLampBlock.LIT))) {
                        player.displayClientMessage(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)).append(Component.literal("  " + machine.dimension + " (" + machine.pos.toShortString() + ")").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))), false);
                    }
                }
                for (MachineStatusCommandConfig.Machine machine : permMachineList) {
                    BlockState blockState = getDimension(player.getServer(), machine.dimension).getBlockState(machine.pos);
                    if ((blockState.getBlock() instanceof LeverBlock && !blockState.getValue(LeverBlock.POWERED)) || (blockState.getBlock() instanceof RedstoneLampBlock && !blockState.getValue(RedstoneLampBlock.LIT))) {
                        player.displayClientMessage(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)).append("  " + machine.dimension + " (" + machine.pos.toShortString() + ")").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)), false);
                    }
                }
                for (MachineStatusCommandConfig.Machine machine : permMachineList) {
                    BlockState blockState = getDimension(player.getServer(), machine.dimension).getBlockState(machine.pos);
                    if (!(blockState.getBlock() instanceof LeverBlock || blockState.getBlock() instanceof RedstoneLampBlock)) {
                        player.displayClientMessage(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)).append(translateComponent("  " + machine.dimension + " (" + machine.pos.toShortString() + ")" + "开关状态未知")), false);
                    }
                }
            }
            return 1;
        }
    }

    public static boolean getAllItemStatus() {
        for (MachineStatusCommandConfig.Machine machine : permMachineList) {
            if (Objects.equals(machine.name, "全物品")) {
                BlockState blockState = getDimension(CarpetServer.minecraft_server, machine.dimension).getBlockState(machine.pos);
                if ((blockState.getBlock() instanceof LeverBlock && blockState.getValue(LeverBlock.POWERED)) || (blockState.getBlock() instanceof RedstoneLampBlock && blockState.getValue(RedstoneLampBlock.LIT))) {
                    return true;
                }
            }
        }
        return false;
    }
}
