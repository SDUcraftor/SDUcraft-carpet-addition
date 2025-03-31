package top.sducraft.config;

import net.minecraft.server.MinecraftServer;
import top.sducraft.config.rule.easyfakePeaceConfig;
import top.sducraft.config.rule.machineStatusCommandConfig;

public class loadConfig {
    public static void load(MinecraftServer server) {
            easyfakePeaceConfig.init(server);
            machineStatusCommandConfig.init(server);
    }
}
