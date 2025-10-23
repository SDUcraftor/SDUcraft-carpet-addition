package top.sducraft.commands.setSignUrl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;


import static carpet.utils.Translations.tr;
import static top.sducraft.util.Message.translateComponent;

public class SetSignUrlCommand {
            public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
                            commandDispatcher.register(Commands.literal("setsignurl")
                                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                    .then(Commands.argument("url", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                ServerLevel level = context.getSource().getLevel();
                                                BlockEntity  blockEntity = level.getBlockEntity(BlockPosArgument.getBlockPos(context, "pos"));
                                                if (blockEntity instanceof SignBlockEntity signBlockEntity) {
                                                    SignText signText = new SignText();
                                                           signText = signText.setMessage(0,Component.literal(signBlockEntity.getFrontText().getMessage(0,false).getString())
                                                                     .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withClickEvent(new ClickEvent.RunCommand("/openurl " + (StringArgumentType.getString(context,"url"))))));
                                                signBlockEntity.setText(signText,true);
                                                context.getSource().sendSuccess(()->Component.literal(tr("sducarpet.command.setSignUrlSuccess") + StringArgumentType.getString(context , "url")),false);
                                                    return 1;
                                                }
                                                else {
                                                context.getSource().sendFailure(translateComponent("sducarpet.command.setSignUrlFailure"));
                                                    return 0;
                                                }
                                            }))));
                        }
}
