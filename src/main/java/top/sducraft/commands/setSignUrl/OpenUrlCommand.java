package top.sducraft.commands.setSignUrl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.net.URI;
import java.net.URISyntaxException;

import static carpet.utils.Translations.tr;

public class OpenUrlCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register(Commands.literal("openurl")
                .then(Commands.argument("url", StringArgumentType.greedyString())
                        .executes(context -> {
                            Player player = context.getSource().getPlayer();
                            if (player != null) {
                                try {
                                    player.displayClientMessage(Component.literal(tr("sducarpet.command.openurl")
                                    ).withStyle(Style.EMPTY.withClickEvent(new ClickEvent.OpenUrl(new URI(StringArgumentType.getString(context, "url"))))
                                            .withBold(true)
                                            .withColor(ChatFormatting.AQUA)), false);
                                } catch (URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                            return 1;
                        })));
    }
}
