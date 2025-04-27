package top.sducraft.helpers.commands.easyFakePeace;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.config.rule.easyfakePeaceConfig;

import static net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock.getConnectedDirection;
import static top.sducraft.config.rule.easyfakePeaceConfig.getFakePeaceStates;
import static top.sducraft.helpers.rule.chunkLoadHelper.RegistTicket.addFakepeaceTicket;

public class easyFakePeaceCommandHelper {

    public static int setFakePeaceState(CommandSourceStack source, ServerLevel dimension, boolean state) {
        String dimensionKey = dimension.dimension().toString();
        String dimensionName = null;
        ServerLevel targetDimension = null;
        switch (dimensionKey) {
            case "ResourceKey[minecraft:dimension / minecraft:overworld]" -> {
                targetDimension = source.getServer().getLevel(Level.NETHER);
                dimensionName = "主世界";
            }
            case "ResourceKey[minecraft:dimension / minecraft:the_nether]" -> {
                targetDimension = source.getServer().getLevel(Level.OVERWORLD);
                dimensionName = "地  狱";
            }
            case "ResourceKey[minecraft:dimension / minecraft:the_end]" -> {
                targetDimension = source.getServer().getLevel(Level.END);
                dimensionName = "末  地";
            }
        }
        BlockPos pos = easyfakePeaceConfig.getFakePeaceCoordinates(dimensionKey);
        if (pos == null) {
            source.sendFailure(Component.literal(dimensionName+"伪和平还没人做啊啊啊啊"));
            return 1;
        }
        if (targetDimension != null) {
            addFakepeaceTicket(targetDimension,new ChunkPos(pos));
        }
        if (source!=null) {
            BlockState blockState = targetDimension.getBlockState(pos);
            if (blockState.getBlock() instanceof LeverBlock) {
                targetDimension.setBlock(pos, blockState.setValue(LeverBlock.POWERED, state), 3);
                targetDimension.updateNeighborsAt(pos, blockState.getBlock());
                targetDimension.updateNeighborsAt(pos.relative(getConnectedDirection(blockState).getOpposite()),blockState.getBlock());
                easyfakePeaceConfig.setFakePeaceState(dimensionKey, state);
                PlayerList playerList = source.getServer().getPlayerList();
                for (ServerPlayer player : playerList.getPlayers()) {
                    if (player != null) {
                        if (dimensionName != null) {
                            String stateName;
                            if (state) {
                                stateName = "开启";
                            } else {
                                stateName = "关闭 这需要一定的时间~";
                            }
                            player.displayClientMessage(Component.literal(dimensionName).append("伪和平").append("已").append(stateName), false);
                        }
                    }
                }
            } else {
                source.sendFailure(Component.literal("该位置的方块不是拉杆！"));
                return 1;
            }
        }
        return 0;
    }

    public static void showFakePeaceStatus(ServerPlayer player) {
        if(SDUcraftCarpetSettings.easyFakePeace) {
            player.displayClientMessage(Component.translatable("sducarpet.easycommand.fakepeacestatus").withStyle(ChatFormatting.BOLD), false);
            player.displayClientMessage(createStateText("主世界", "minecraft:overworld", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:overworld]")), false);
            player.displayClientMessage(createStateText("地  狱", "minecraft:the_nether", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:the_nether]")), false);
            player.displayClientMessage(createStateText("末  地", "minecraft:the_end", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:the_end]")), false);
        }
    }

    private static Component createStateText(String dimensionName, String dimensionKey, Boolean state) {
        Component trueComponent;
        Component falseComponent;
        if (state) {
            trueComponent = Component.literal("[true] ")
                    .withStyle(Style.EMPTY.withBold(true).withUnderlined(true).withColor(ChatFormatting.AQUA));
            falseComponent = Component.literal("[false]")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fakepeace " + dimensionKey + " false"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击关闭").append(dimensionName).append("伪和平")))
                            .withColor(ChatFormatting.GRAY));
        } else {
            trueComponent = Component.literal("[true] ")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/fakepeace " + dimensionKey + " true"))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击开启").append(dimensionName).append("伪和平")))
                            .withColor(ChatFormatting.GRAY));
            falseComponent = Component.literal("[false]")
                    .withStyle(Style.EMPTY.withBold(true).withUnderlined(true).withColor(ChatFormatting.AQUA));
        }
        return Component.literal(dimensionName + "伪和平  ")
                .append(trueComponent)
                .append(falseComponent);
    }
}
