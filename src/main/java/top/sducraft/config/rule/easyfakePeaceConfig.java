package top.sducraft.config.rule;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.LevelResource;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class easyfakePeaceConfig {
    public static File configFile;
    static Map<String, FakePeaceData> fakePeaceData = new HashMap<>();

    static class FakePeaceData {
        int x;
        int y;
        int z;
        boolean state;
        public FakePeaceData(int x, int y, int z, boolean state) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.state = state;
        }
    }

    // 初始化时加载配置
    public static void init(MinecraftServer server) {
        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        File configDir = new File(worldDir, "config/carpet-sducraft-addition");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "fake_peace_config.json");
        loadConfig();
    }

    // 加载配置文件
    private static void loadConfig() {
        try {
            if (configFile.exists()) {
                FileReader reader = new FileReader(configFile);
                Type type = new TypeToken<Map<String, FakePeaceData>>() {}.getType();
                fakePeaceData = new Gson().fromJson(reader, type);
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // 保存配置文件
    public static void saveConfig() {
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(fakePeaceData, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BlockPos getFakePeaceCoordinates(String dimension) {
        FakePeaceData data = fakePeaceData.get(dimension);
        if (data != null) {
            return new BlockPos(data.x, data.y, data.z);
        }
        return null;
    }

    public static boolean getFakePeaceStates(String dimension) {
        FakePeaceData data = fakePeaceData.get(dimension);
        if (data != null) {
            return data.state;
        }
        return false;
    }

    public static int setFakePeaceCoordinates(String dimension, int x,int y ,int z) {
        FakePeaceData data = new FakePeaceData(x, y,z, false);
        fakePeaceData.put(dimension, data);
        saveConfig();
        return 1;
    }

    public static void setFakePeaceState(String dimension, boolean state) {
        FakePeaceData data = fakePeaceData.getOrDefault(dimension, new FakePeaceData(0, 0, 0, state));
        data.state = state;
        fakePeaceData.put(dimension, data);
        saveConfig();
    }
}