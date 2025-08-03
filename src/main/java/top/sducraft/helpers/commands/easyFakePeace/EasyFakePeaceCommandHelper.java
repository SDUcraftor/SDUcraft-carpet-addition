package top.sducraft.helpers.commands.easyFakePeace;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.CommandStorage;
import top.sducraft.SDUcraftCarpetSettings;
import top.sducraft.config.rule.EasyFakePeaceConfig;

import static carpet.utils.Translations.tr;
import static net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock.getConnectedDirection;
import static top.sducraft.config.rule.EasyFakePeaceConfig.getFakePeaceStates;
import static top.sducraft.helpers.rule.chunkLoadHelper.RegistTicket.addFakepeaceTicket;
import static top.sducraft.util.SandMessage.sandAllPlayerCustomMessage;

public class EasyFakePeaceCommandHelper {

    public static void showPeacefulStatusDialog(ServerPlayer player) {

        // 1. 在 Java 中动态构建 JSON 对象
        JsonObject dialogJson = new JsonObject();
        dialogJson.addProperty("type", "multi_action");
        dialogJson.addProperty("columns", 2);

        // 标题
        JsonObject title = new JsonObject();
        title.addProperty("text", "伪和平状态控制 (实时)");
        dialogJson.add("title", title);

        // 动态创建的按钮
        JsonArray actions = new JsonArray();
        // --- 主世界 ---
        addDimensionButtons(actions, "主世界", "minecraft:overworld",
                getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:overworld]"));
        // --- 下界 ---
        addDimensionButtons(actions, "下界", "minecraft:the_nether",
                getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:the_nether]"));
        // --- 末地 ---
        addDimensionButtons(actions, "末地", "minecraft:the_end",
                getFakePeaceStates("ResourceKey[minecraft:dimension / minecraft:the_end]"));

        dialogJson.add("actions", actions);

        // 返回按钮
        JsonObject exitAction = new JsonObject();
        JsonObject exitLabel = new JsonObject();
        exitLabel.addProperty("text", "返回");
        exitAction.add("label", exitLabel);
        JsonObject exitActionDetails = new JsonObject();
        exitActionDetails.addProperty("type", "show_dialog");
        // 注意：返回按钮仍然需要打开一个已注册的dialog
        exitActionDetails.addProperty("value", "sducarpet:fakepeace_main");
        exitAction.add("action", exitActionDetails);
        dialogJson.add("exit_action", exitAction);

        // 2. 将 JSON 对象转换为字符串
        String jsonString = dialogJson.toString();

        // 3. 构建并执行 /dialog show 命令
        String command = "dialog show " + player.getName().getString() + " " + jsonString;
        MinecraftServer server = player.getServer();
        server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command);
    }

    private static void addDimensionButtons(JsonArray actionsArray, String dimensionId, String refreshCommand, boolean currentState) {
        // --- 开启按钮 ---
        JsonObject onButton = createActionButton(
                "开启",
                "/fakepeace " + dimensionId + " true",
                refreshCommand,
                currentState // 如果当前已开启，则禁用此按钮
        );

        // --- 关闭按钮 ---
        JsonObject offButton = createActionButton(
                "关闭",
                "/fakepeace " + dimensionId + " false",
                refreshCommand,
                !currentState // 如果当前已关闭，则禁用此按钮
        );

        actionsArray.add(onButton);
        actionsArray.add(offButton);
    }

    /**
     * (私有辅助方法) 创建一个通用的操作按钮 JSON 对象。
     * (已修正，使用正确的 "command" 键)
     */
    private static JsonObject createActionButton(String labelText, String actionCommand, String refreshCommand, boolean isDisabled) {
        JsonObject button = new JsonObject();

        JsonObject label = new JsonObject();
        label.addProperty("text", labelText);
        if (isDisabled) {
            label.addProperty("color", "gray");
            label.addProperty("italic", true);
        }
        button.add("label", label);

        // 使用正确的结构: "action": { "type": "...", "command": "..." }
        JsonObject action = new JsonObject();
        action.addProperty("type", "run_command");
        action.addProperty("command", actionCommand);
        button.add("action", action);

        // 点击后刷新对话框以显示最新状态
        JsonObject afterAction = new JsonObject();
        afterAction.addProperty("type", "run_command");
        afterAction.addProperty("command", refreshCommand);
        button.add("after_action", afterAction);

        return button;
    }


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
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/fakepeace " + dimensionKey + " false"))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击关闭").append(dimensionName).append("伪和平")))
                            .withColor(ChatFormatting.GRAY));
        } else {
            trueComponent = Component.literal("[true] ")
                    .withStyle(Style.EMPTY.withClickEvent(new ClickEvent.RunCommand("/fakepeace " + dimensionKey + " true"))
                            .withHoverEvent(new HoverEvent.ShowText(Component.literal("点击开启").append(dimensionName).append("伪和平")))
                            .withColor(ChatFormatting.GRAY));
            falseComponent = Component.literal("[false]")
                    .withStyle(Style.EMPTY.withBold(true).withUnderlined(true).withColor(ChatFormatting.AQUA));
        }
        return Component.literal(dimensionName + "伪和平  ")
                .append(trueComponent)
                .append(falseComponent);
    }
}
