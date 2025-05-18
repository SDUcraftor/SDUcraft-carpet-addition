package top.sducraft.config;

import net.minecraft.server.MinecraftServer;
import top.sducraft.config.allItemData.AllItemData;
import top.sducraft.config.rule.WarningConfig;
import top.sducraft.config.rule.EasyFakePeaceConfig;
import top.sducraft.config.rule.MachineStatusCommandConfig;

public class LoadConfig {
    public static void load(MinecraftServer server) {
            EasyFakePeaceConfig.init(server);
            MachineStatusCommandConfig.init(server);
            WarningConfig.init(server);
            AllItemData.init(server);
    }
}
