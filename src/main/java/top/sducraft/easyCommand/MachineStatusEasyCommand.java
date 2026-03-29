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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static top.sducraft.config.rule.MachineStatusCommandConfig.permMachineList;
import static top.sducraft.config.rule.MachineStatusCommandConfig.tempMachineList;
import static top.sducraft.util.Message.translateComponent;

public class MachineStatusEasyCommand implements IEasyCommand {
    private enum MachineState {
        ON,
        OFF,
        UNKNOWN
    }

    @Override
    public String getCommandName() {
        return "machinestatus";
    }

    private static ServerLevel getDimension(MinecraftServer server, String dimension) {
        ServerLevel level = null;
        switch (dimension) {
            case "overworld" -> level = server.getLevel(ServerLevel.OVERWORLD);
            case "nether" -> level = server.getLevel(ServerLevel.NETHER);
            case "end" -> level = server.getLevel(ServerLevel.END);
            case "the_nether" -> level = server.getLevel(ServerLevel.NETHER);
            case "the_end" -> level = server.getLevel(ServerLevel.END);
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
        boolean noTempMachines = tempMachineList.isEmpty();
        boolean noPermMachines = permMachineList.isEmpty();
        if (noTempMachines && noPermMachines) {
            player.displayClientMessage(translateComponent("sducarpet.easycommand.machinestatus1"), false);
            return 0;
        }

        player.displayClientMessage(translateComponent("sducarpet.easycommand.machinestatus4").append(translateComponent("sducarpet.easycommand.machinestatus5").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))).append(translateComponent("sducarpet.easycommand.machinestatus6")).append(translateComponent("sducarpet.easycommand.machinestatus7").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))).append(translateComponent("sducarpet.easycommand.machinestatus8")), false);
        if (!noTempMachines) {
            player.displayClientMessage(translateComponent("sducarpet.easycommand.machinestatus2"), false);
            for (MachineStatusCommandConfig.Machine machine : tempMachineList) {
                player.displayClientMessage(formatMachineLine(machine, ChatFormatting.GREEN, ChatFormatting.WHITE), false);
            }
        }

        if (!noPermMachines) {
            player.displayClientMessage(translateComponent("sducarpet.easycommand.machinestatus3"), false);
            List<Component> onMachines = new ArrayList<>();
            List<Component> offMachines = new ArrayList<>();
            List<Component> unknownMachines = new ArrayList<>();

            for (MachineStatusCommandConfig.Machine machine : permMachineList) {
                switch (getMachineState(player.getServer(), machine)) {
                    case ON -> onMachines.add(formatMachineLine(machine, ChatFormatting.GREEN, ChatFormatting.WHITE));
                    case OFF -> offMachines.add(formatMachineLine(machine, ChatFormatting.GRAY, ChatFormatting.GRAY));
                    case UNKNOWN -> unknownMachines.add(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)).append(translateComponent("  " + machine.dimension + " (" + machine.pos.toShortString() + ")开关状态未知")));
                }
            }

            for (Component line : onMachines) {
                player.displayClientMessage(line, false);
            }
            for (Component line : offMachines) {
                player.displayClientMessage(line, false);
            }
            for (Component line : unknownMachines) {
                player.displayClientMessage(line, false);
            }
        }

        return 1;
    }

    public static boolean getAllItemStatus() {
        for (MachineStatusCommandConfig.Machine machine : permMachineList) {
            if (Objects.equals(machine.name, "全物品")) {
                if (getMachineState(CarpetServer.minecraft_server, machine) == MachineState.ON) {
                    return true;
                }
            }
        }
        return false;
    }

    private static Component formatMachineLine(MachineStatusCommandConfig.Machine machine, ChatFormatting nameColor, ChatFormatting detailColor) {
        return Component.literal(machine.name).withStyle(Style.EMPTY.withColor(nameColor))
                .append(Component.literal("  " + machine.dimension + " (" + machine.pos.toShortString() + ")").withStyle(Style.EMPTY.withColor(detailColor)));
    }

    private static MachineState getMachineState(MinecraftServer server, MachineStatusCommandConfig.Machine machine) {
        ServerLevel level = getDimension(server, machine.dimension);
        if (level == null) {
            return MachineState.UNKNOWN;
        }

        BlockState blockState = level.getBlockState(machine.pos);
        if (blockState.getBlock() instanceof LeverBlock) {
            return blockState.getValue(LeverBlock.POWERED) ? MachineState.ON : MachineState.OFF;
        }
        if (blockState.getBlock() instanceof RedstoneLampBlock) {
            return blockState.getValue(RedstoneLampBlock.LIT) ? MachineState.ON : MachineState.OFF;
        }
        return MachineState.UNKNOWN;
    }
}
