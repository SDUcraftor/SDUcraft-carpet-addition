package top.sducraft.helpers.commands.qqInformationCollect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class QqInformationCollector {
    public static File dataFile;
    public static List<qqData> qqDataList = new LinkedList<>();

    public static class qqData {
        public String name;
        public String uuid;
        public int qqNumber;

        public qqData(String name, String uuid, int qqNumber) {
            this.name = name;
            this.uuid = uuid;
            this.qqNumber = qqNumber;
        }
    }

    public static void init(MinecraftServer server) {
        File configDir = FabricLoader.getInstance().getGameDir().toFile();
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        dataFile = new File(configDir, "qq.json");
        loadData();
    }

    private static void loadData() {
        try {
            if (dataFile.exists()) {
                FileReader reader = new FileReader(dataFile);
                Type type = new TypeToken<List<qqData>>() {
                }.getType();
                qqDataList = new Gson().fromJson(reader, type);
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        try {
            FileWriter writer = new FileWriter(dataFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(qqDataList, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addData(ServerPlayer player, int qqNumber) {
        String name = player.getName().getString();
        String uuid = player.getStringUUID();

        for (qqData data : qqDataList) {
            if (data.uuid.equals(uuid)) {
                data.qqNumber = qqNumber; // 更新 QQ 号
                saveData();
                return;
            }
        }

        qqData newData = new qqData(name, uuid, qqNumber);
        qqDataList.add(newData);
        saveData();
    }

}