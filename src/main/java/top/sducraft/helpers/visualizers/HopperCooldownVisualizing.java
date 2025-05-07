package top.sducraft.helpers.visualizers;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Predicate;


public class HopperCooldownVisualizing {
    public static Map<BlockPos, Display.TextDisplay> visualizers = new HashMap<>();

    public static void setVisualizer(ServerLevel world, BlockPos pos, int cooldown) {
        if (visualizers.containsKey(pos)) {
            Display.TextDisplay entity = visualizers.get(pos);
            CompoundTag nbt = entity.saveWithoutId(new CompoundTag());
            String color = cooldown == 0 ? "green" : "red";
            String textJson = "{\"text\":\"" + cooldown + "\",\"color\":\"" + color + "\"}";
            nbt.remove("text");
            nbt.putString("text", textJson);
            entity.load(nbt);
        } else {
            Display.TextDisplay entity = new Display.TextDisplay(EntityType.TEXT_DISPLAY, world);
            entity.setInvisible(true);
            entity.setNoGravity(true);
            entity.setInvulnerable(true);
            entity.setPos(pos.getCenter().x(), pos.getCenter().y(), pos.getCenter().z());
            entity.addTag("hopperCooldownVisualizer");
            world.addFreshEntity(entity);
            CompoundTag nbt = entity.saveWithoutId(new CompoundTag());
            String color = cooldown == 0 ? "green" : "red";
            nbt.putString("billboard", "center");
            String textJson = "{\"text\":\"" + String.valueOf(cooldown) + "\",\"color\":\"" + color + "\"}";
            nbt.putByte("see_through", (byte) 1);
            nbt.putInt("background", 0x00000000);
            nbt.putString("text", textJson);
            entity.load(nbt);
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
            Predicate<Display.TextDisplay> predicate = marker -> marker.getTags().contains("hopperCooldownVisualizer");
            level.getEntities(EntityType.TEXT_DISPLAY,
                    predicate,
                    entities);
            entities.forEach(Entity::discard);
        }
    }
}
