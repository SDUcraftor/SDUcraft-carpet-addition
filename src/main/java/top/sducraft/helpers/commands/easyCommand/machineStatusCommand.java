package top.sducraft.helpers.commands.easyCommand;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.sducraft.config.rule.machineStatusCommandConfig;
import static top.sducraft.config.rule.machineStatusCommandConfig.permMachineList;
import static top.sducraft.config.rule.machineStatusCommandConfig.tempMachineList;
import static top.sducraft.util.massageComponentCreate.createCommandClickComponent;

public class machineStatusCommand implements IEasyCommand {
    @Override
    public String getCommandName() {
        return "machinestatus";
    }

    @Override
    public Component clickButton() {
        return createCommandClickComponent("[机器状态查询]", "/easycommand machinestatus","点击进入机器状态查询界面");
    }

    @Override
    public void showEasyCommandInterface(ServerPlayer player) {
        Component component = Component.literal("\n[machine指令介绍]\n").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))
//                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mcdreforged.com/zh-CN/plugin/gamemode"))
//                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击查看!!spec命令使用文档"))))
                .append(Component.literal("""
                        /machine status 查看当前机器状态
                        /machine add <temp/perm> <name> <dimension> <blockPos> 添加机器
                        /machine remove <temp/perm> <name> 删除机器>
                        非临时机器会自动获取坐标处开关状态，临时机器默认为开，使用完毕请删除(服务器关闭后临时机器不会保存)
                        """))
                .append(Component.literal("注意事项:非op仅能添加/删除临时机器").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)));
        player.displayClientMessage(component, false);
    }

    private static ServerLevel getDimension(MinecraftServer server, String dimension) {
        ServerLevel level = null;
        switch (dimension) {
            case "overworld"-> level = server.getLevel(ServerLevel.OVERWORLD);
            case "nether"-> level = server.getLevel(ServerLevel.NETHER);
            case "end"-> level = server.getLevel(ServerLevel.END);
        }
        return level;
    }

    public static int showMachineStatus(ServerPlayer player){
        boolean bl1 = tempMachineList.isEmpty();
        boolean bl2 = permMachineList.isEmpty();
        if (bl1 && bl2){
            player.displayClientMessage(Component.translatable("sducarpet.easycommand.machinestatus1"),false);
            return 0;
        }
        else {
            player.displayClientMessage(Component.translatable("sducarpet.easycommand.machinestatus4").append(Component.translatable("sducarpet.easycommand.machinestatus5").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))).append(Component.translatable("sducarpet.easycommand.machinestatus6")).append(Component.translatable("sducarpet.easycommand.machinestatus7").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY))).append(Component.translatable("sducarpet.easycommand.machinestatus8")), false);
            if (!bl1) {
                player.displayClientMessage(Component.translatable("sducarpet.easycommand.machinestatus2"), false);
                for (machineStatusCommandConfig.Machine machine : tempMachineList) {
                    player.displayClientMessage(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)).append(Component.literal("  " + machine.dimension + " (" + machine.pos.toShortString() + ")").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))), false);
                }
            }
            if (!bl2) {
                player.displayClientMessage(Component.translatable("sducarpet.easycommand.machinestatus3"), false);
                for (machineStatusCommandConfig.Machine machine : permMachineList) {
                    BlockState blockState = getDimension(player.getServer(), machine.dimension).getBlockState(machine.pos);
                    if ((blockState.getBlock() instanceof LeverBlock && blockState.getValue(LeverBlock.POWERED)) || (blockState.getBlock() instanceof RedstoneLampBlock && blockState.getValue(RedstoneLampBlock.LIT))) {
                        player.displayClientMessage(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)).append(Component.literal("  " + machine.dimension + " (" + machine.pos.toShortString() + ")").withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE))), false);
                    }
                }
                for (machineStatusCommandConfig.Machine machine : permMachineList) {
                    BlockState blockState = getDimension(player.getServer(), machine.dimension).getBlockState(machine.pos);
                    if ((blockState.getBlock() instanceof LeverBlock && !blockState.getValue(LeverBlock.POWERED)) || (blockState.getBlock() instanceof RedstoneLampBlock && !blockState.getValue(RedstoneLampBlock.LIT))) {
                        player.displayClientMessage(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)).append("  " + machine.dimension + " (" + machine.pos.toShortString() + ")").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)), false);
                    }
                }
                for (machineStatusCommandConfig.Machine machine : permMachineList) {
                    BlockState blockState = getDimension(player.getServer(), machine.dimension).getBlockState(machine.pos);
                    if (!(blockState.getBlock() instanceof LeverBlock || blockState.getBlock() instanceof RedstoneLampBlock)) {
                        player.displayClientMessage(Component.literal(machine.name).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)).append("  " + machine.dimension + " (" + machine.pos.toShortString() + ")" + "开关状态未知"), false);
                    }
                }
            }
            return 1;
        }
    }
}
