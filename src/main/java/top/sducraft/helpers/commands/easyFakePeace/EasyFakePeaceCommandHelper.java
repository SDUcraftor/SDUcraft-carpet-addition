package top.sducraft.helpers.commands.easyFakePeace;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.config.rule.EasyFakePeaceConfig;
import top.sducraft.util.dialog.ActionBuilder;
import top.sducraft.util.dialog.MultiActionDialogBuilder;
import top.sducraft.util.dialog.TextComponentBuilder;

import static carpet.utils.Translations.tr;
import static net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock.getConnectedDirection;
import static top.sducraft.config.rule.EasyFakePeaceConfig.getFakePeaceStates;
import static top.sducraft.helpers.rule.chunkLoadHelper.RegistTicket.addFakepeaceTicket;
import static top.sducraft.util.Message.sandAllPlayerCustomMessage;
import static top.sducraft.util.Message.sandPlayerDialog;

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
                                sandAllPlayerCustomMessage(source.getServer(),dimensionName+tr("sducarpet.easycommand.fakepeace3"),ChatFormatting.WHITE,300);
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

//        Component component = createDescriptionClickComponent("\n[伪和平原理简介]在Minecraft中,一个维度的刷怪数量存在上限(粗略计算方法为生存模式玩家数量*70),所以只要提前准备足够数量的怪物就可以在某个维度实现'和平'的效果",
//                "https://zh.minecraft.wiki/w/Tutorial:%E4%BC%AA%E5%92%8C%E5%B9%B3?variant=zh-cn",
//                "点击查看伪和平原理详细介绍","注意事项:在开关伪和平前请告知其他玩家,以免发生意外,非必要不要关闭伪和平\n");
//        player.displayClientMessage(component,false);
//        showFakePeaceStatus(player);

        if(SDUcraftCarpetSettings.easyFakePeace) {
            MultiActionDialogBuilder dialogBuilder = new MultiActionDialogBuilder();

            dialogBuilder.setTitle("伪和平助手")
                    .setColumns(3)
                    .addExitButton("关闭");

            TextComponentBuilder text = new TextComponentBuilder();
            text.append("伪和平原理简介:在Minecraft中,一个维度的刷怪数量存在上限(粗略计算方法为生存模式玩家数量*70)\n所以只要提前准备足够数量的怪物就可以在某个维度实现'和平'的效果").withWidth(500);
            text.append("\n注意事项:在开关伪和平前请告知其他玩家,以免发生意外,非必要不要关闭伪和平").color("red");
            dialogBuilder.addBody(text);

            dialogBuilder.addAction(createStateText(tr("sducarpet.easycommand.fakepeacestatus1"), "minecraft:overworld", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:overworld]")));
            dialogBuilder.addAction(createStateText(tr("sducarpet.easycommand.fakepeacestatus2"), "minecraft:the_nether", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:the_nether]")));
            dialogBuilder.addAction(createStateText(tr("sducarpet.easycommand.fakepeacestatus3"), "minecraft:the_end", getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:the_end]")));

            dialogBuilder.addAction(new ActionBuilder("伪和平介绍")
                    .withTooltip("点击查看伪和平详细介绍")
                    .asOpenUrl("https://zh.minecraft.wiki/w/Tutorial:%E4%BC%AA%E5%92%8C%E5%B9%B3?variant=zh-cn"));

            sandPlayerDialog(player, dialogBuilder);
        }
    }

    private static ActionBuilder createStateText(String dimensionName, String dimensionKey, Boolean state) {
        ActionBuilder actionBuilder;
        TextComponentBuilder text = new TextComponentBuilder();
        if (state) {
            text.append("关闭" + dimensionName + "伪和平").color("green");
            actionBuilder = ActionBuilder.withComplexLabel(text).asRunCommand("/fakepeace " + dimensionKey + " false").withTooltip("点击关闭" + dimensionName + "伪和平");
        } else {
            text.append("开启" + dimensionName + "伪和平").color("red");
            actionBuilder = ActionBuilder.withComplexLabel(text).asRunCommand("/fakepeace " + dimensionKey + " true").withTooltip("点击开启" + dimensionName + "伪和平");

        }

        return actionBuilder;
    }
}
