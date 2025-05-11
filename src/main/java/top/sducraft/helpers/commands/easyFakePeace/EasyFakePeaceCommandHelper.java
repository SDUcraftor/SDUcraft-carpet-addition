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
import top.sducraft.config.rule.EasyFakePeaceConfig;

import static carpet.utils.Translations.tr;
import static net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock.getConnectedDirection;
import static top.sducraft.config.rule.EasyFakePeaceConfig.getFakePeaceStates;
import static top.sducraft.helpers.rule.chunkLoadHelper.RegistTicket.addFakepeaceTicket;
import static top.sducraft.util.SandMessage.sandAllPlayerCustomMessage;

public class EasyFakePeaceCommandHelper {

    public static int setFakePeaceState(CommandSourceStack source, ServerLevel dimension, boolean state) {
        String dimensionKey = dimension.dimension().toString();
        String dimensionName = null;
        ServerLevel targetDimension = null;
        switch (dimensionKey) {
            case "ResourceKey[minecraft:dimension / minecraft:overworld]" -> {
                targetDimension = source.getServer().getLevel(Level.NETHER);
                dimensionName = tr("sducarpet.easycommand.fakepeacestatus1");
            }
            case "ResourceKey[minecraft:dimension / minecraft:the_nether]" -> {
                targetDimension = source.getServer().getLevel(Level.OVERWORLD);
                dimensionName = tr("sducarpet.easycommand.fakepeacestatus2");
            }
            case "ResourceKey[minecraft:dimension / minecraft:the_end]" -> {
                targetDimension = source.getServer().getLevel(Level.END);
                dimensionName = tr("sducarpet.easycommand.fakepeacestatus3");
            }
        }
        BlockPos pos = EasyFakePeaceConfig.getFakePeaceCoordinates(dimensionKey);
        if (pos == null) {
            source.sendFailure(Component.literal(dimensionName+"伪和平还没人做啊啊啊啊"));
            return 1;
        }
        if (targetDimension != null) {
            addFakepeaceTicket(targetDimension,new ChunkPos(pos));
        }
        if (source!=null && targetDimension!=null) {
            BlockState blockState = targetDimension.getBlockState(pos);
            if (blockState.getBlock() instanceof LeverBlock) {
                targetDimension.setBlock(pos, blockState.setValue(LeverBlock.POWERED, state), 3);
                targetDimension.updateNeighborsAt(pos, blockState.getBlock());
                targetDimension.updateNeighborsAt(pos.relative(getConnectedDirection(blockState).getOpposite()),blockState.getBlock());
                EasyFakePeaceConfig.setFakePeaceState(dimensionKey, state);
                        if (dimensionName != null) {
                            if (state) {
                                sandAllPlayerCustomMessage(source.getServer(),dimensionName+tr("sducarpet.easycommand.fakepeace1"),ChatFormatting.WHITE);
                            } else {
                                sandAllPlayerCustomMessage(source.getServer(),dimensionName+tr("sducarpet.easycommand.fakepeace2"),ChatFormatting.WHITE);
                                sandAllPlayerCustomMessage(source.getServer(),dimensionName+tr("sducarpet.easycommand.fakepeace3"),ChatFormatting.WHITE,15000);
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
            player.displayClientMessage(Component.literal(tr("sducarpet.easycommand.fakepeacestatus")).withStyle(ChatFormatting.BOLD), false);
            player.displayClientMessage(createStateText(tr("sducarpet.easycommand.fakepeacestatus1"), "minecraft:overworld", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:overworld]")), false);
            player.displayClientMessage(createStateText(tr("sducarpet.easycommand.fakepeacestatus2"), "minecraft:the_nether", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:the_nether]")), false);
            player.displayClientMessage(createStateText(tr("sducarpet.easycommand.fakepeacestatus3"), "minecraft:the_end", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:the_end]")), false);
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
