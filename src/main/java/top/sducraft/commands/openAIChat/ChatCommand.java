package top.sducraft.commands.openAIChat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import top.sducraft.helpers.chat.ChatMemory;

import static carpet.utils.Translations.tr;
import static top.sducraft.helpers.chat.OpenaiChat.tryStartChat;

public class ChatCommand {
            public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
                            commandDispatcher.register(Commands.literal("chat")
                                    .then(Commands.literal("clear")
                                            .executes(context -> {
                                                ServerPlayer player = context.getSource().getPlayer();
                                                if (player != null) {
                                                    ChatMemory.clear(player);
                                                    context.getSource().sendSuccess(() -> Component.literal(tr("sducarpet.command.chat1")), false);
                                                    return 1;
                                                }
                                                context.getSource().sendFailure(Component.literal(tr("sducarpet.command.chat2")));
                                                return 0;
                                            }))
                                    .then(Commands.argument("content", StringArgumentType.greedyString())
                                            .executes(context -> {
                                                ServerPlayer player = context.getSource().getPlayer();
                                                if (player != null) {
                                                    tryStartChat(StringArgumentType.getString(context,"content"),player);
                                                    return 1;
                                                }
                                                return 0;
                                            })));
            }
}
