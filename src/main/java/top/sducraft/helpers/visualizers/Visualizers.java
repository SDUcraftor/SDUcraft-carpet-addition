package top.sducraft.helpers.visualizers;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import top.sducraft.SDUcraftCarpetSettings;

public class Visualizers {
    public static void updateVisualizers(MinecraftServer server) {
        if(server != null) {
            HopperCooldownVisualizing.clearVisualizers(server);
        }
    }
}
