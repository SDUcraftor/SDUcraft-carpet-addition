package top.sducraft.config.findItemArea;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class FindItemAreaData {
    private static File configFile;
    private static final Map<String, AreaData> areas = new HashMap<>();

    public static class AreaData {
        public double minX, minY, minZ;
        public double maxX, maxY, maxZ;

        public AreaData(BlockPos from, BlockPos to) {
            this.minX = Math.min(from.getX(), to.getX());
            this.minY = Math.min(from.getY(), to.getY());
            this.minZ = Math.min(from.getZ(), to.getZ());
            this.maxX = Math.max(from.getX(), to.getX()) + 1.0; // +1 to be inclusive
            this.maxY = Math.max(from.getY(), to.getY()) + 1.0;
            this.maxZ = Math.max(from.getZ(), to.getZ()) + 1.0;
        }

        public AABB toAABB() {
            return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }

    public static void init(MinecraftServer server) {
        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        File configDir = new File(worldDir, "data/carpet-sducraft-addition");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "finditem_areas.json");
        loadConfig();
    }

    private static void loadConfig() {
        try (FileReader reader = new FileReader(configFile)) {
            if (configFile.exists()) {
                Type type = new TypeToken<HashMap<String, AreaData>>() {
                }.getType();
                Map<String, AreaData> loadedData = new Gson().fromJson(reader, type);
                if (loadedData != null) {
                    areas.clear();
                    areas.putAll(loadedData);
                }
            }
        } catch (Exception e) {
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(configFile)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(areas, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, AreaData> getAreas() {
        return new HashMap<>(areas); // Return a copy to prevent outside modification
    }

    public static boolean addArea(String name, AreaData data) {
        if (areas.containsKey(name)) return false;
        areas.put(name, data);
        saveConfig();
        return true;
    }

    public static boolean removeArea(String name) {
        if (areas.remove(name) == null) return false;
        saveConfig();
        return true;
    }
}