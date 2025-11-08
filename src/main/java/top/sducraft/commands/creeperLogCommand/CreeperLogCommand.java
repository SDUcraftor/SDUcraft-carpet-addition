package top.sducraft.commands.creeperLogCommand;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;

import java.io.File;
import java.util.Arrays;
import java.util.stream.Stream;

import static top.sducraft.helpers.visualizers.CreeperLogVisualizer.visualizeLog;

public class CreeperLogCommand {
    
    private static final SuggestionProvider<CommandSourceStack> LOG_FILE_SUGGESTIONS = (context, builder) -> {
        File logDir = context.getSource().getServer()
                .getWorldPath(LevelResource.ROOT)
                .resolve("data")
                .resolve("creeperlog")
                .toFile();
        
        if (logDir.exists() && logDir.isDirectory()) {
            File[] files = logDir.listFiles((dir, name) -> name.endsWith(".log"));
            if (files != null) {
                Stream<String> fileNames = Arrays.stream(files)
                        .map(File::getName)
                        .map(name -> name.substring(0, name.length() - 4))
                        .map(name -> "\"" + name +"\"");
                return SharedSuggestionProvider.suggest(fileNames, builder);
            }
        }
        return builder.buildFuture();
    };
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("creeperlog")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("log", StringArgumentType.string())
                        .suggests(LOG_FILE_SUGGESTIONS)
                        .executes(CreeperLogCommand::executeVisualize)));
    }
    
    private static int executeVisualize(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String logFileName = StringArgumentType.getString(context, "log");
        
        try {
            ServerLevel level = source.getLevel();
            File logFile = source.getServer()
                    .getWorldPath(LevelResource.ROOT)
                    .resolve("data")
                    .resolve("creeperlog")
                    .resolve(logFileName + ".log")
                    .toFile();
            
            if (!logFile.exists()) {
                source.sendFailure(Component.literal("日志文件不存在: " + logFileName));
                return 0;
            }
            
            visualizeLog(level, logFile, source);
            source.sendSuccess(() -> Component.literal("已可视化苦力怕爆炸日志: " + logFileName), true);
            return 1;
            
        } catch (Exception e) {
            source.sendFailure(Component.literal("读取日志文件失败: " + e.getMessage()));
            e.printStackTrace();
            return 0;
        }
    }
}

