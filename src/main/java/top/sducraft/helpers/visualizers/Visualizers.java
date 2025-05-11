package top.sducraft.helpers.visualizers;

import net.minecraft.server.MinecraftServer;
import java.util.Timer;
import java.util.TimerTask;

public class Visualizers {
    public static void updateVisualizers(MinecraftServer server) {
        if(server != null) {
            HopperCooldownVisualizing.clearVisualizers(server);
        }
    }

    public static void clearVisualizersOnServerStart(MinecraftServer server) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    HopperCooldownVisualizing.clearVisualizers(server);
                    timer.cancel();}}, 500);
    }
}
