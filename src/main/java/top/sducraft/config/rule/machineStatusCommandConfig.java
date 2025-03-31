package top.sducraft.config.rule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class machineStatusCommandConfig {
    public static File configFile;
    public static List<Machine> permMachineList = new ArrayList<>();
    public static List<Machine> tempMachineList = new ArrayList<>();

    public static class Machine {
        public BlockPos pos;
        public String name;
        public String dimension;

        public Machine(BlockPos pos, String name, String dimension) {
            this.pos = pos;
            this.name = name;
            this.dimension = dimension;
        }
    }

    public static void init(MinecraftServer server) {
        File worldDir = server.getWorldPath(LevelResource.ROOT).toFile();
        File configDir = new File(worldDir, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        configFile = new File(configDir, "machine.json");
        loadConfig();
    }

    private static void loadConfig() {
        try {
            if (configFile.exists()) {
                FileReader reader = new FileReader(configFile);
                Type type = new TypeToken<List<Machine>>() {}.getType();
                permMachineList = new Gson().fromJson(reader, type);
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveConfig() {
        try {
            FileWriter writer = new FileWriter(configFile);
            new GsonBuilder().setPrettyPrinting().create().toJson(permMachineList, writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addPermMachine(String name,BlockPos pos,String dimension) {
        permMachineList.add(new Machine(pos, name,dimension));
        saveConfig();
    }

    public static void addTempMachine(String name,BlockPos pos,String dimension) {
        tempMachineList.add(new Machine(pos, name,dimension));
    }

    public static void delPermMachine(String name, ServerPlayer player) {
        if(Objects.equals(name, "all"))
        {
            permMachineList.clear();
            player.displayClientMessage(Component.literal("已删除所有机器"),false);
        }
        else {
            if(permMachineList.removeIf(machine -> machine.name.equals(name))){
                player.displayClientMessage(Component.literal("已删除"+name),false);
            };
        }
        saveConfig();
    }

    public static void delTempMachine(String name,ServerPlayer player) {
        if(Objects.equals(name, "all"))
        {
            tempMachineList.clear();
            player.displayClientMessage(Component.literal("已删除所有临时机器"),false);
        }
        else {
            if(tempMachineList.removeIf(machine -> machine.name.equals(name))){
                player.displayClientMessage(Component.literal("已删除"+name),false);
            };
        }
        saveConfig();
    }
}