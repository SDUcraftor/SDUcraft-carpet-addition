package top.sducraft.config.rule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.system.NonnullDefault;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class alertConfig {
    public static File configFile;
    public static List<alert> alertList = new ArrayList<>();

    public static class alert {
        public BlockPos pos;
        public String name;
        public String dimension;
        public String text;
        public boolean status;

        public alert(String name,BlockPos pos,String dimension,String text, boolean status) {
            this.pos = pos;
            this.name = name;
            this.dimension = dimension;
            this.text = text;
            this.status = status;
        }
    }

    public static void init(MinecraftServer server) {
        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        File configDir = new File(worldDir, "config/carpet-sducraft-addition");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "alert.json");
        loadConfig();
    }

    private static void loadConfig() {
        try {
            if (configFile.exists()) {
                FileReader reader = new FileReader(configFile);
                Type type = new TypeToken<List<alert>>() {}.getType();
                alertList = new Gson().fromJson(reader, type);
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(alertList, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean addAlert(String name,String dimension, BlockPos pos, String text, Boolean status) {
        for (alert alert : alertList) {
            if (Objects.equals(alert.name, name)){
                return false;
            }
        }
        alertList.add(new alert(name,pos,dimension,text, Boolean.TRUE.equals(status)));
        saveConfig();
        return true;
    }

    public static boolean delAlert(String name, ServerPlayer player) {
        if (Objects.equals(name, "all")) {
            alertList.clear();
            saveConfig();
            return true;
        } else if (alertList.removeIf(alert -> alert.name.equals(name))) {
            saveConfig();
            return true;
        } else {
            return false;
        }
    }

    public static boolean setAlert(String name, boolean status) {
        boolean isUpdated = false;
        for (alert alert : alertList) {
            if (alert.name.equals(name)) {
                alert.status = status;
                isUpdated = true;
            }
        }
        if (isUpdated) {
            saveConfig();
            return true;
        }
        return false;
    }

}