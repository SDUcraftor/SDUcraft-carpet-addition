package top.sducraft.config.allItemData;

import carpet.CarpetServer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import top.sducraft.util.DelayedEvents;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;
import static top.sducraft.helpers.commands.allItemCommand.SpawnDisplay.generateDisplaysInfo;
import static top.sducraft.helpers.translation.allitem.ItemTranslation.translateItem;

public class AllItemData {
    public static File configFile;
    public static HashMap<String, ItemData> dataList = new HashMap<>();
    public static HashMap<String, ItemData> chineseNameToData = new HashMap<>();
    public static HashMap<String, ItemData> englishNameToData = new HashMap<>();

    public static class ItemData {
        public String type;
        public HashSet<BlockPos> storePos;
        public HashSet<BlockPos> chestPos;

        public ItemData(String type, HashSet<BlockPos> storePos, HashSet<BlockPos> chestPos) {
            this.storePos = storePos;
            this.type = type;
            this.chestPos = chestPos;
        }
    }

    public static void init(MinecraftServer server) {
        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        File configDir = new File(worldDir, "data/carpet-sducraft-addition");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "allitemdata.json");
        loadConfig();
    }

    private static void loadConfig() {
        try {
            if (configFile.exists()) {
                FileReader reader = new FileReader(configFile);
                Type type = new TypeToken<HashMap<String, ItemData>>() {}.getType();
                dataList = new Gson().fromJson(reader, type);
                reader.close();
                updateNameToDataMap();
                DelayedEvents.START_SERVER_TICK.register(20, s ->generateDisplaysInfo(CarpetServer.minecraft_server.overworld()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        updateNameToDataMap();
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(dataList, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ✅ 添加物品
    public static void addItem(String key,String type,BlockPos pos,HashSet<BlockPos> storePos) {
        ItemData data = dataList.get(key);
        if (data != null) {
            data.storePos.add(pos);
        } else {
            HashSet<BlockPos> set = new HashSet<>();
            set.add(pos);
            dataList.put(key, new ItemData(type,set,storePos));
        }
        saveConfig();
    }

    public static void addItem(String key,String type,HashSet<BlockPos> storePos,HashSet<BlockPos> chestPos) {
        ItemData data = dataList.get(key);
            if (data != null) {
                data.storePos.addAll(storePos);
                data.chestPos.addAll(chestPos);
            } else {
                dataList.put(key, new ItemData(type, storePos, chestPos));
            }
        saveConfig();
    }

    public static void delItem(String key) {
        dataList.remove(key);
        saveConfig();
    }

    public static List<String> fuzzySearch(String keyword) {
        List<String> result = new ArrayList<>();

        for (Map.Entry<String, ItemData> entry : chineseNameToData.entrySet()) {
            if (entry.getKey().toLowerCase().contains(keyword)) {
                    result.add(entry.getKey());
            }
        }

        for (Map.Entry<String, ItemData> entry : englishNameToData.entrySet()) {
            if (entry.getKey().toLowerCase().contains(keyword)) {
                    result.add(entry.getKey());
            }
        }

        return result;
    }

    public static ItemData search(String keyword){
        ItemData byChinese = chineseNameToData.get(keyword);
        if (byChinese != null) return byChinese;
        return englishNameToData.get(keyword);
    }

    private static String stripPrefix(String key) {
        if (key.startsWith("item.minecraft.")) {
            return key.substring("item.minecraft.".length());
        } else if (key.startsWith("block.minecraft.")) {
            return key.substring("block.minecraft.".length());
        }
        return key;
    }

    private static void updateNameToDataMap() {
        chineseNameToData.clear();
        englishNameToData.clear();
        for (Map.Entry<String, ItemData> entry : dataList.entrySet()) {
            String key = entry.getKey();
            String chineseName = translateItem(key);
            String englishName = stripPrefix(key);
            chineseNameToData.put(chineseName, entry.getValue());
            englishNameToData.put(englishName, entry.getValue());
        }
    }
}
