package top.sducraft.helpers.visualizers;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
//#if MC >= 260200
import static net.minecraft.world.entity.EntityTypes.BLOCK_DISPLAY;
import static net.minecraft.world.entity.EntityTypes.ITEM_DISPLAY;
import static net.minecraft.world.entity.EntityTypes.TEXT_DISPLAY;
//#else
//$$ import static net.minecraft.world.entity.EntityType.BLOCK_DISPLAY;
//$$ import static net.minecraft.world.entity.EntityType.ITEM_DISPLAY;
//$$ import static net.minecraft.world.entity.EntityType.TEXT_DISPLAY;
//#endif

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static com.mojang.text2speech.Narrator.LOGGER;


public class HopperCooldownVisualizing {
    public static Map<BlockPos, Display.TextDisplay> visualizers = new HashMap<>();

    public static void setVisualizer(ServerLevel level, BlockPos pos, int cooldown) {
        if (visualizers.containsKey(pos)) {
            Display.TextDisplay entity = visualizers.get(pos);
            ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER);
            TagValueOutput nbt = TagValueOutput.createWithContext(scopedCollector, entity.registryAccess());
            entity.saveWithoutId(nbt);
            String color = cooldown == 0 ? "green" : "red";
            String textJson = "{\"text\":\"" + cooldown + "\",\"color\":\"" + color + "\"}";
            nbt.discard("text");
            nbt.putString("text", textJson);
            entity.load(TagValueInput.create(scopedCollector, entity.registryAccess(), nbt.buildResult()));
        } else {
            Display.TextDisplay entity = new Display.TextDisplay(TEXT_DISPLAY, level);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            //#if MC >= 260300
            //$$ entity.setPermanentlyInvulnerable(true);
            //#else
            entity.setInvulnerable(true);
            //#endif
            entity.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            entity.addTag("hopperCooldownVisualizer");
            entity.addTag("noSave");
            level.addFreshEntity(entity);
            ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER);
            TagValueOutput nbt = TagValueOutput.createWithContext(scopedCollector, entity.registryAccess());
            entity.saveWithoutId(nbt);
            String color = cooldown == 0 ? "green" : "red";
            nbt.putString("billboard", "center");
            String textJson = "{\"text\":\"" + String.valueOf(cooldown) + "\",\"color\":\"" + color + "\"}";
            nbt.putByte("see_through", (byte) 1);
            nbt.putInt("background", 0x00000000);
            nbt.putString("text", textJson);
            entity.load(TagValueInput.create(scopedCollector, entity.registryAccess(), nbt.buildResult()));
            visualizers.put(pos, entity);
        }
    }

    public static void removeVisualizer(BlockPos pos) {
        if (visualizers.containsKey(pos)) {
            Display.TextDisplay entity = visualizers.get(pos);
            entity.discard();
            visualizers.remove(pos);
        }
    }

    public static void clearVisualizers(MinecraftServer server) {
        visualizers.clear();
        clearLevelVisualizers(server.overworld());
        clearLevelVisualizers(server.getLevel(Level.NETHER));
        clearLevelVisualizers(server.getLevel(Level.END));
    }

    public static void clearLevelVisualizers(ServerLevel level) {
        if (level != null) {
            List<Display.TextDisplay> entities = new ArrayList<>();
            Predicate<Display.TextDisplay> predicate = marker -> marker.entityTags().contains("hopperCooldownVisualizer");
            level.getEntities(TEXT_DISPLAY,
                    predicate,
                    entities);
            entities.forEach(Entity::discard);
        }
    }
}
