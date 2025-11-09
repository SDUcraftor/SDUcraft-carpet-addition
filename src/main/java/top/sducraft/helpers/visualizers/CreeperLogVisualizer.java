package top.sducraft.helpers.visualizers;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import top.sducraft.util.DelayedEvents;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.text2speech.Narrator.LOGGER;
import static top.sducraft.util.Message.translateComponent;

public class CreeperLogVisualizer {
    
    private static final String TAG_CREEPER_LOG = "creeper_log_visualization";
    private static final int DURATION_TICKS = 1200; // 60秒
    private static final float DISPLAY_SCALE = 0.3f;
    private static final int COLOR_SLIME = 0x00FF00; // 绿色（史莱姆方块）
    private static final int COLOR_WOOL = 0xFFFF00; // 黄色（羊毛）
    private static final int COLOR_TEXT = 0xFFFFFF; // 白色（文字）
    
    private static class LogEntry {
        int sequence;
        Vec3 creeperPos;
        Vec3 playerPos;
        long gameTime;
        String playerName;
        
        public LogEntry(int sequence, Vec3 creeperPos, Vec3 playerPos, long gameTime, String playerName) {
            this.sequence = sequence;
            this.creeperPos = creeperPos;
            this.playerPos = playerPos;
            this.gameTime = gameTime;
            this.playerName = playerName;
        }
    }
    
    public static void visualizeLog(ServerLevel level, File logFile, CommandSourceStack source) throws IOException {
        clearPreviousVisualizations(level);
        DelayedEvents.START_SERVER_TICK.register(5, s -> {
            List<LogEntry> entries = parseLogFile(logFile);

            if (entries.isEmpty()) {
                source.sendFailure(translateComponent("sducarpet.command.creeperlog.noData"));
                return;
            }

            for (LogEntry entry : entries) {
                spawnBlockDisplayAt(level, entry.creeperPos, Blocks.SLIME_BLOCK.defaultBlockState(),
                        COLOR_SLIME, DISPLAY_SCALE);

                if (entry.playerPos != null) {
                    spawnBlockDisplayAt(level, entry.playerPos, Blocks.YELLOW_WOOL.defaultBlockState(),
                            COLOR_WOOL, DISPLAY_SCALE);
                }
                Vec3 textPos = entry.creeperPos.add(0, 0.5, 0);
                spawnTextDisplayAt(level, textPos, String.valueOf(entry.sequence));

                if (entry.playerPos != null) {
                    Vec3 playerTextPos = entry.playerPos.add(0, 0.5, 0);
                    spawnTextDisplayAt(level, playerTextPos, String.valueOf(entry.sequence));
                }
            }

            source.sendSuccess(() -> translateComponent("sducarpet.command.creeperlog.visualized")
                    .append(" " + entries.size() + " ")
                    .append(translateComponent("sducarpet.command.creeperlog.recordsCount")), false);
        });
    }
    
    private static List<LogEntry> parseLogFile(File logFile) throws IOException {
        List<LogEntry> entries = new ArrayList<>();

        Pattern pattern = Pattern.compile(
                "Time: (\\d+) \\| Target: ([^|]+) \\| Creeper Pos: \\[([0-9.\\-, ]+)\\](?: \\| Player Pos: (?:N/A|\\[([0-9.\\-, ]+)\\]))?"
        );
        
        int sequence = 1;
        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    long gameTime = Long.parseLong(matcher.group(1));
                    String playerName = matcher.group(2).trim();
                    String creeperPosStr = matcher.group(3);
                    String playerPosStr = matcher.group(4);
                    
                    Vec3 creeperPos = parseVec3(creeperPosStr);
                    Vec3 playerPos = playerPosStr != null ? parseVec3(playerPosStr) : null;
                    
                    entries.add(new LogEntry(sequence++, creeperPos, playerPos, gameTime, playerName));
                }
            }
        }
        
        return entries;
    }
    
    private static Vec3 parseVec3(String str) {
        String[] parts = str.split(",");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid Vec3 format: " + str);
        }
        double x = Double.parseDouble(parts[0].trim());
        double y = Double.parseDouble(parts[1].trim());
        double z = Double.parseDouble(parts[2].trim());
        return new Vec3(x, y, z);
    }
    
    private static void spawnBlockDisplayAt(ServerLevel level, Vec3 pos, BlockState blockState, 
                                           int color, float scale) {
        Display.BlockDisplay display = new Display.BlockDisplay(EntityType.BLOCK_DISPLAY, level);
        display.setBlockState(blockState);
        display.setPos(pos);
        display.setGlowingTag(true);
        display.getEntityData().set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, color);
        display.getEntityData().set(Display.DATA_SCALE_ID, new Vector3f(scale));
        display.addTag("noSave");
        display.addTag(TAG_CREEPER_LOG);
        level.addFreshEntity(display);

        DelayedEvents.START_SERVER_TICK.register(DURATION_TICKS, s -> {
            if (!display.isRemoved()) {
                display.discard();
            }
        });
    }
    
    private static void spawnTextDisplayAt(ServerLevel level, Vec3 pos, String text) {
        Display.TextDisplay display = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        display.setPos(pos);
        display.setText(Component.literal(text));
        display.setGlowingTag(true);
        display.addTag(TAG_CREEPER_LOG);
        display.setInvisible(true);
        display.setNoGravity(true);
        display.addTag("noSave");
        display.setInvulnerable(true);
        display.getEntityData().set(Display.DATA_GLOW_COLOR_OVERRIDE_ID, CreeperLogVisualizer.COLOR_TEXT);
        level.addFreshEntity(display);

        ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(display.problemPath(), LOGGER);
        TagValueOutput nbt = TagValueOutput.createWithContext(scopedCollector, display.registryAccess());
        display.saveWithoutId(nbt);
        nbt.putString("billboard", "center");
        nbt.putByte("see_through", (byte) 1);
        nbt.putInt("background", 0x00000000);
        display.load(TagValueInput.create(scopedCollector, display.registryAccess(), nbt.buildResult()));

        DelayedEvents.START_SERVER_TICK.register(DURATION_TICKS, s -> {
            if (!display.isRemoved()) {
                display.discard();
            }
        });
    }
    
    private static void clearPreviousVisualizations(ServerLevel level) {
        level.getAllEntities().forEach(entity -> {
            if (entity instanceof Display && entity.getTags().contains(TAG_CREEPER_LOG)) {
                DelayedEvents.START_SERVER_TICK.register(1, s -> {
                        entity.discard();
                });
            }
        });
    }
}

